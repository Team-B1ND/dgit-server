package dodam.b1nd.dgit.domain.github.account.controller

import dodam.b1nd.dgit.domain.github.account.dto.request.RegisterGithubAccountRequest
import dodam.b1nd.dgit.domain.github.account.dto.response.GithubAccountResponse
import dodam.b1nd.dgit.domain.github.account.service.GitHubAccountService
import dodam.b1nd.dgit.global.response.ApiResponse
import dodam.b1nd.dgit.global.security.UserAuthenticationHolder
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
class GitHubAccountController(
    private val githubAccountService: GitHubAccountService
) {

    @Operation(
        summary = "Github 계정 등록",
        description = """
            Github 아이디로 계정을 등록합니다. 이미 등록된 경우 에러를 반환합니다.

            **데이터 수집 방식:**
            - 계정 등록 즉시 GitHub GraphQL API를 사용하여 사용자의 커밋 데이터를 수집합니다.
            - GraphQL을 통해 커밋 날짜만 선택적으로 가져와 응답 크기를 99% 감소시킵니다. (REST API 대비 약 300배 효율적)
            - 수집 데이터: 사용자 정보, 전체 커밋 수, 오늘 커밋 수, 이번 주 커밋 수, 최장/현재 스트릭

            **참고:**
            - 첫 등록 시 데이터 수집에 몇 초 정도 소요될 수 있습니다.
            - 이후 매시간(07:00-23:00) 자동으로 데이터가 갱신됩니다.
        """.trimIndent(),
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @PostMapping("/register")
    fun registerGithubAccount(
        @Valid @RequestBody request: RegisterGithubAccountRequest
    ): ApiResponse<GithubAccountResponse> {
        val user = UserAuthenticationHolder.current()
        val response = githubAccountService.registerGithubAccount(user, request)

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
    fun getMyGithubAccounts(): ApiResponse<List<GithubAccountResponse>> {
        val user = UserAuthenticationHolder.current()
        val response = githubAccountService.getMyGithubAccounts(user)

        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "Github 계정 목록 조회 성공",
            data = response
        )
    }
}
