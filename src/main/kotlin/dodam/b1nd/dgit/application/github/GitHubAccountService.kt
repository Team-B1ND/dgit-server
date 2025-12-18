package dodam.b1nd.dgit.application.github

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.domain.user.entity.User
import dodam.b1nd.dgit.infrastructure.exception.CustomException
import dodam.b1nd.dgit.infrastructure.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GitHubAccountService(
    private val githubAccountRepository: GithubAccountRepository
) {

    @Transactional
    fun register(user: User, username: String): GithubAccount {
        if (githubAccountRepository.existsByUsername(username)) {
            throw CustomException(ErrorCode.GITHUB_USERNAME_ALREADY_TAKEN)
        }

        val githubAccount = GithubAccount(
            user = user,
            username = username
        )

        return githubAccountRepository.save(githubAccount)
    }

    fun getMyAccounts(user: User): List<GithubAccount> {
        return githubAccountRepository.findAllByUser(user)
    }
}
