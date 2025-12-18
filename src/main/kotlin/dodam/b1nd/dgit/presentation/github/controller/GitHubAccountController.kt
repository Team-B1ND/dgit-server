package dodam.b1nd.dgit.presentation.github.controller

import dodam.b1nd.dgit.application.github.usecase.GitHubAccountUseCase
import dodam.b1nd.dgit.infrastructure.security.UserAuthenticationHolder
import dodam.b1nd.dgit.presentation.common.ApiResponse
import dodam.b1nd.dgit.presentation.github.controller.docs.GitHubAccountControllerDocs
import dodam.b1nd.dgit.presentation.github.dto.request.RegisterGithubAccountRequest
import dodam.b1nd.dgit.presentation.github.dto.response.GithubAccountResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/github")
class GitHubAccountController(
    private val gitHubAccountUseCase: GitHubAccountUseCase
) : GitHubAccountControllerDocs {

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @PostMapping("/register")
    override fun registerGithubAccount(
        @Valid @RequestBody request: RegisterGithubAccountRequest
    ): ApiResponse<GithubAccountResponse> {
        val user = UserAuthenticationHolder.current()
        val response = gitHubAccountUseCase.registerGithubAccount(user, request)

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
        val response = gitHubAccountUseCase.getMyGithubAccounts(user)

        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "Github 계정 목록 조회 성공",
            data = response
        )
    }
}
