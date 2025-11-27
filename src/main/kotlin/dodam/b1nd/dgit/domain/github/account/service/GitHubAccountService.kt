package dodam.b1nd.dgit.domain.github.account.service

import dodam.b1nd.dgit.domain.github.account.dto.request.RegisterGithubAccountRequest
import dodam.b1nd.dgit.domain.github.account.dto.response.GithubAccountResponse
import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.domain.user.service.UserService
import dodam.b1nd.dgit.global.exception.CustomException
import dodam.b1nd.dgit.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GithubAccountService(
    private val githubAccountRepository: GithubAccountRepository,
    private val userService: UserService
) {

    /**
     * Github 계정 등록
     * @param email 사용자 이메일 (JWT에서 추출)
     * @param request Github 계정 등록 요청
     * @return Github 계정 응답
     */
    @Transactional
    fun registerGithubAccount(email: String, request: RegisterGithubAccountRequest): GithubAccountResponse {
        val user = userService.getUserByEmail(email)

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

    fun getMyGithubAccounts(email: String): List<GithubAccountResponse> {
        val user = userService.getUserByEmail(email)
        val githubAccounts = githubAccountRepository.findAllByUser(user)

        return githubAccounts.map { GithubAccountResponse.from(it) }
    }
}
