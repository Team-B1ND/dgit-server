package dodam.b1nd.dgit.application.github

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.domain.github.stats.entity.GithubStats
import dodam.b1nd.dgit.domain.github.stats.repository.GithubStatsRepository
import dodam.b1nd.dgit.infrastructure.client.GithubClient
import dodam.b1nd.dgit.infrastructure.exception.CustomException
import dodam.b1nd.dgit.infrastructure.exception.ErrorCode
import org.springframework.stereotype.Service
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

    @Transactional
    fun updateUserStats(account: GithubAccount) {
        val userInfo = githubClient.getUser(account.username)
        account.name = userInfo.name
        account.bio = userInfo.bio
        account.avatarUrl = userInfo.avatarUrl

        val stats = githubStatsRepository.findByGithubAccount(account)
            ?: GithubStats(githubAccount = account)

        updateStatsFromGithub(stats, account.username)
        githubStatsRepository.save(stats)
    }

    fun getStats(githubAccountId: Long): GithubStats {
        val githubAccount = githubAccountRepository.findById(githubAccountId).orElseThrow {
            CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
        }

        return githubStatsRepository.findByGithubAccount(githubAccount)
            ?: throw CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
    }

    fun getAccountRanking(githubAccountId: Long): Int {
        val githubAccount = githubAccountRepository.findById(githubAccountId).orElseThrow {
            CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
        }

        val stats = githubStatsRepository.findByGithubAccount(githubAccount)
            ?: throw CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)

        val ranking = githubStatsRepository.countByTotalCommitsGreaterThan(stats.totalCommits)
        return (ranking + 1).toInt()
    }

    fun getAccountPercentile(githubAccountId: Long): Double {
        val ranking = getAccountRanking(githubAccountId)
        val totalUsers = githubStatsRepository.count()
        if (totalUsers == 0L) return 0.0
        return (ranking.toDouble() / totalUsers.toDouble()) * 100.0
    }

    private fun updateStatsFromGithub(stats: GithubStats, username: String) {
        val repositories = githubClient.getUserRepositories(username)
        stats.repositoryCount = repositories.size

        val allCommitDates = githubClient.getAllUserCommitDates(username)

        val sortedDates = allCommitDates.sorted()
        stats.totalCommits = sortedDates.size

        val today = LocalDate.now()
        stats.todayCommits = sortedDates.count { it == today }

        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        stats.weekCommits = sortedDates.count { it in startOfWeek..endOfWeek }

        val streaks = calculateStreaks(sortedDates)
        stats.longestStreak = streaks.first
        stats.currentStreak = streaks.second
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
