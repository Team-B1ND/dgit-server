package dodam.b1nd.dgit.domain.github.account.controller

import dodam.b1nd.dgit.domain.github.account.dto.request.RegisterGithubAccountRequest
import dodam.b1nd.dgit.domain.github.account.dto.response.GithubAccountResponse
import dodam.b1nd.dgit.domain.github.account.service.GithubAccountService
import dodam.b1nd.dgit.global.response.ApiResponse
import dodam.b1nd.dgit.global.security.JwtProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Github Account", description = "Github 계정 관리 API")
@RestController
@RequestMapping("/github")
class GithubAccountController(
    private val githubAccountService: GithubAccountService,
    private val jwtProvider: JwtProvider
) {

    /**
     * POST /github/register
     * Github 계정 등록
     */
    @Operation(
        summary = "Github 계정 등록",
        description = "Github 아이디로 계정을 등록합니다. 이미 등록된 경우 에러를 반환합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @PostMapping("/register")
    fun registerGithubAccount(
        @RequestHeader("Authorization") authorization: String,
        @Valid @RequestBody request: RegisterGithubAccountRequest
    ): ApiResponse<GithubAccountResponse> {
        val token = authorization.substring(7) // "Bearer " 제거
        val email = jwtProvider.getEmailFromToken(token)

        val response = githubAccountService.registerGithubAccount(email, request)

        return ApiResponse.success(
            status = HttpStatus.CREATED,
            message = "Github 계정 등록 성공",
            data = response
        )
    }

    @Operation(
        summary = "내 Github 계정 목록 조회",
        description = "현재 로그인한 사용자의 모든 Github 계정을 조회합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/me")
    fun getMyGithubAccounts(
        @RequestHeader("Authorization") authorization: String
    ): ApiResponse<List<GithubAccountResponse>> {
        val token = authorization.substring(7)
        val email = jwtProvider.getEmailFromToken(token)

        val response = githubAccountService.getMyGithubAccounts(email)

        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "Github 계정 목록 조회 성공",
            data = response
        )
    }
}
