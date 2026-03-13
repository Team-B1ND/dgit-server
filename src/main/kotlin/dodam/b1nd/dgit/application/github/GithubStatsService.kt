package dodam.b1nd.dgit.application.github

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.domain.github.stats.entity.GithubStats
import dodam.b1nd.dgit.domain.github.stats.repository.GithubStatsRepository
import dodam.b1nd.dgit.infrastructure.client.GithubClient
import dodam.b1nd.dgit.infrastructure.exception.CustomException
import dodam.b1nd.dgit.infrastructure.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Service
@Transactional(readOnly = true)
class GithubStatsService(
    private val githubStatsRepository: GithubStatsRepository,
    private val githubAccountRepository: GithubAccountRepository,
    private val githubClient: GithubClient
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateUserStats(account: GithubAccount) {
        val userData = githubClient.fetchUserData(account.username)

        account.name = userData.name
        account.bio = userData.bio
        account.avatarUrl = userData.avatarUrl

        val stats = githubStatsRepository.findByGithubAccount(account)
            ?: GithubStats(githubAccount = account)

        stats.repositoryCount = userData.repositoryCount
        stats.totalCommits = userData.totalCommits

        val today = LocalDate.now()
        stats.todayCommits = userData.contributionDays
            .firstOrNull { it.date == today }?.count ?: 0

        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        stats.weekCommits = userData.contributionDays
            .filter { it.date in startOfWeek..endOfWeek }
            .sumOf { it.count }

        val activeDates = userData.contributionDays
            .filter { it.count > 0 }
            .map { it.date }
            .sorted()

        val streaks = calculateStreaks(activeDates)
        stats.longestStreak = streaks.first
        stats.currentStreak = streaks.second

        githubStatsRepository.save(stats)
    }

    fun getStats(githubAccountId: Long): GithubStats {
        val githubAccount = githubAccountRepository.findById(githubAccountId).orElseThrow {
            CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
        }

        return githubStatsRepository.findByGithubAccount(githubAccount)
            ?: GithubStats(githubAccount = githubAccount)
    }

    fun getAccountRanking(githubAccountId: Long): Int {
        val githubAccount = githubAccountRepository.findById(githubAccountId).orElseThrow {
            CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
        }

        val stats = githubStatsRepository.findByGithubAccount(githubAccount)
            ?: return (githubStatsRepository.count() + 1).toInt()

        val ranking = githubStatsRepository.countByTotalCommitsGreaterThan(stats.totalCommits)
        return (ranking + 1).toInt()
    }

    fun getAccountPercentile(githubAccountId: Long): Double {
        val ranking = getAccountRanking(githubAccountId)
        val totalUsers = githubStatsRepository.count()
        if (totalUsers == 0L) return 0.0
        return (ranking.toDouble() / totalUsers.toDouble()) * 100.0
    }

    private fun calculateStreaks(commitDates: List<LocalDate>): Pair<Int, Int> {
        if (commitDates.isEmpty()) return Pair(0, 0)

        val uniqueDates = commitDates.toSet().sortedDescending()

        var longestStreak = 0
        var currentStreakCount = 0

        val today = LocalDate.now()
        if (uniqueDates.first() == today || uniqueDates.first() == today.minusDays(1)) {
            var streak = 1
            var previousDate = uniqueDates.first()

            for (i in 1 until uniqueDates.size) {
                val date = uniqueDates[i]
                if (previousDate.minusDays(1) == date) {
                    streak++
                    previousDate = date
                } else {
                    break
                }
            }
            currentStreakCount = streak
        }

        var tempStreak = 1
        var previousDate = uniqueDates.first()

        for (i in 1 until uniqueDates.size) {
            val date = uniqueDates[i]
            if (previousDate.minusDays(1) == date) {
                tempStreak++
            } else {
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 1
            }
            previousDate = date
        }
        longestStreak = maxOf(longestStreak, tempStreak)

        return Pair(longestStreak, currentStreakCount)
    }
}
