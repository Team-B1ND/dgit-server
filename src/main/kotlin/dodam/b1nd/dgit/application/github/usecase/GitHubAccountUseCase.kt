package dodam.b1nd.dgit.application.github.usecase

import dodam.b1nd.dgit.application.github.GitHubAccountService
import dodam.b1nd.dgit.application.github.GithubStatsService
import dodam.b1nd.dgit.domain.user.entity.User
import dodam.b1nd.dgit.presentation.github.dto.request.RegisterGithubAccountRequest
import dodam.b1nd.dgit.presentation.github.dto.response.GithubAccountResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GitHubAccountUseCase(
    private val gitHubAccountService: GitHubAccountService,
    private val githubStatsService: GithubStatsService
) {

    @Transactional
    fun registerGithubAccount(user: User, request: RegisterGithubAccountRequest): GithubAccountResponse {
        val savedAccount = gitHubAccountService.register(user, request.username)

        try {
            githubStatsService.updateUserStats(savedAccount)
        } catch (e: Exception) {
        }

        return GithubAccountResponse.from(savedAccount)
    }

    fun getMyGithubAccounts(user: User): List<GithubAccountResponse> {
        val githubAccounts = gitHubAccountService.getMyAccounts(user)
        return githubAccounts.map { GithubAccountResponse.from(it) }
    }
}
