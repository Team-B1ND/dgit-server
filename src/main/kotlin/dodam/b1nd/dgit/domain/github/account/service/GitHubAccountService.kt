package dodam.b1nd.dgit.domain.github.account.service

import dodam.b1nd.dgit.domain.github.account.dto.request.RegisterGithubAccountRequest
import dodam.b1nd.dgit.domain.github.account.dto.response.GithubAccountResponse
import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.domain.user.entity.User
import dodam.b1nd.dgit.global.exception.CustomException
import dodam.b1nd.dgit.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GitHubAccountService(
    private val githubAccountRepository: GithubAccountRepository
) {

    /**
     * Github 계정 등록
     * @param user 현재 인증된 사용자
     * @param request Github 계정 등록 요청
     * @return Github 계정 응답
     */
    @Transactional
    fun registerGithubAccount(user: User, request: RegisterGithubAccountRequest): GithubAccountResponse {
        if (githubAccountRepository.existsByUsername(request.username)) {
            throw CustomException(ErrorCode.GITHUB_USERNAME_ALREADY_TAKEN)
        }

        val githubAccount = GithubAccount(
            user = user,
            username = request.username
        )

        val savedAccount = githubAccountRepository.save(githubAccount)
        return GithubAccountResponse.from(savedAccount)
    }

    fun getMyGithubAccounts(user: User): List<GithubAccountResponse> {
        val githubAccounts = githubAccountRepository.findAllByUser(user)

        return githubAccounts.map { GithubAccountResponse.from(it) }
    }
}
