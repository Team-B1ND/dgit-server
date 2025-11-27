package dodam.b1nd.dgit.domain.github.fame.service

import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.domain.github.fame.entity.WeeklyRecord
import dodam.b1nd.dgit.domain.github.fame.repository.WeeklyRecordRepository
import dodam.b1nd.dgit.global.client.GithubClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class WeeklyRecordService(
    private val weeklyRecordRepository: WeeklyRecordRepository,
    private val githubAccountRepository: GithubAccountRepository,
    private val githubClient: GithubClient
) {

    @Scheduled(cron = "0 0 0 * * MON")
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

                val repositories = githubClient.getUserRepositories(account.username)
                var weekCommits = 0

                for (repo in repositories) {
                    val commits = githubClient.getRepositoryCommits(
                        owner = account.username,
                        repo = repo.name,
                        author = account.username,
                        since = lastWeekStart
                    )

                    weekCommits += commits.count { commit ->
                        val commitDate = LocalDateTime.parse(
                            commit.commit.author.date,
                            DateTimeFormatter.ISO_DATE_TIME
                        ).toLocalDate()
                        commitDate in lastWeekStart..lastWeekEnd
                    }
                }

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
