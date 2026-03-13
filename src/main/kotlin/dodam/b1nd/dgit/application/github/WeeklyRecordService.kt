package dodam.b1nd.dgit.application.github

import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.domain.github.fame.entity.WeeklyRecord
import dodam.b1nd.dgit.domain.github.fame.repository.WeeklyRecordRepository
import dodam.b1nd.dgit.infrastructure.client.GithubClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class WeeklyRecordService(
    private val weeklyRecordRepository: WeeklyRecordRepository,
    private val githubAccountRepository: GithubAccountRepository,
    private val githubClient: GithubClient
) {

    @Transactional
    fun saveLastWeekRecords() {
        val lastWeekStart = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY)
        val lastWeekEnd = lastWeekStart.plusDays(6)

        val allAccounts = githubAccountRepository.findAll()

        for (account in allAccounts) {
            try {
                if (weeklyRecordRepository.existsByGithubAccountAndWeekStart(account, lastWeekStart)) {
                    continue
                }

                val contributionDays = githubClient.fetchContributionsForDateRange(
                    account.username, lastWeekStart, lastWeekEnd
                )
                val weekCommits = contributionDays.sumOf { it.count }

                if (weekCommits > 0) {
                    val record = WeeklyRecord(
                        githubAccount = account,
                        weekStart = lastWeekStart,
                        weekCommits = weekCommits
                    )
                    weeklyRecordRepository.save(record)
                }
            } catch (_: Exception) {
                continue
            }
        }
    }
}
