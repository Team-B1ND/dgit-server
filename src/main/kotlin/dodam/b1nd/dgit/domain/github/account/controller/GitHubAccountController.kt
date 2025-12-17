package dodam.b1nd.dgit.domain.github.account.controller

import dodam.b1nd.dgit.domain.github.account.controller.docs.GitHubAccountControllerDocs
import dodam.b1nd.dgit.domain.github.account.dto.request.RegisterGithubAccountRequest
import dodam.b1nd.dgit.domain.github.account.dto.response.GithubAccountResponse
import dodam.b1nd.dgit.domain.github.account.service.GitHubAccountService
import dodam.b1nd.dgit.global.response.ApiResponse
import dodam.b1nd.dgit.global.security.UserAuthenticationHolder
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/github")
class GitHubAccountController(
    private val githubAccountService: GitHubAccountService
) : GitHubAccountControllerDocs {

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @PostMapping("/register")
    override fun registerGithubAccount(
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

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/me")
    override fun getMyGithubAccounts(): ApiResponse<List<GithubAccountResponse>> {
        val user = UserAuthenticationHolder.current()
        val response = githubAccountService.getMyGithubAccounts(user)

        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "Github 계정 목록 조회 성공",
            data = response
        )
    }
}
