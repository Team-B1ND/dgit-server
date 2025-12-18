package dodam.b1nd.dgit.application.github.usecase

import dodam.b1nd.dgit.application.github.GithubStatsService
import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class GitHubStatsUseCase(
    private val githubAccountRepository: GithubAccountRepository,
    private val githubStatsService: GithubStatsService
) {

    @Scheduled(cron = "0 0 7-23 * * *")
    fun updateAllStatsScheduled() {
        val allAccounts = githubAccountRepository.findAll()

        for (account in allAccounts) {
            try {
                updateSingleUserStats(account)
            } catch (_: Exception) {
                continue
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateSingleUserStats(account: GithubAccount) {
        githubStatsService.updateUserStats(account)
    }
}
