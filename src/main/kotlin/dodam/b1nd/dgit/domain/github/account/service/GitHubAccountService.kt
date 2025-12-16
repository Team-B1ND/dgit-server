package dodam.b1nd.dgit.domain.github.account.service

import dodam.b1nd.dgit.domain.github.account.dto.request.RegisterGithubAccountRequest
import dodam.b1nd.dgit.domain.github.account.dto.response.GithubAccountResponse
import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.domain.github.stats.service.GithubStatsService
import dodam.b1nd.dgit.domain.user.entity.User
import dodam.b1nd.dgit.global.exception.CustomException
import dodam.b1nd.dgit.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GitHubAccountService(
    private val githubAccountRepository: GithubAccountRepository,
    private val githubStatsService: GithubStatsService
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

        // 계정 등록 즉시 GitHub 데이터 수집
        try {
            githubStatsService.updateSingleUserStats(savedAccount)
        } catch (e: Exception) {
            // 데이터 수집 실패해도 계정 등록은 성공으로 처리
            // 스케줄러가 나중에 다시 시도함
            println("GitHub 데이터 수집 실패: ${e.message}")
            e.printStackTrace()
        }

        return GithubAccountResponse.from(savedAccount)
    }

    fun getMyGithubAccounts(user: User): List<GithubAccountResponse> {
        val githubAccounts = githubAccountRepository.findAllByUser(user)

        return githubAccounts.map { GithubAccountResponse.from(it) }
    }
}
