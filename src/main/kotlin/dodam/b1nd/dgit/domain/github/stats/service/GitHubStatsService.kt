package dodam.b1nd.dgit.domain.github.stats.service

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.domain.github.stats.entity.GithubStats
import dodam.b1nd.dgit.domain.github.stats.repository.GithubStatsRepository
import dodam.b1nd.dgit.global.client.GithubClient
import dodam.b1nd.dgit.global.exception.CustomException
import dodam.b1nd.dgit.global.exception.ErrorCode
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Service
@Transactional(readOnly = true)
class GithubStatsService(
    private val githubStatsRepository: GithubStatsRepository,
    private val githubAccountRepository: GithubAccountRepository,
    private val githubClient: GithubClient
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

    private fun updateStatsFromGithub(stats: GithubStats, username: String) {
        try {
            val repositories = githubClient.getUserRepositories(username)
            stats.repositoryCount = repositories.size

            val allCommitDates = mutableListOf<LocalDate>()

            // REST API 사용: 전체 커밋 정보를 가져옴
            for (repo in repositories) {
                val commits = githubClient.getRepositoryCommits(
                    owner = username,
                    repo = repo.name,
                    author = username
                )
                commits.forEach { commit ->
                    val commitDate = LocalDateTime.parse(
                        commit.commit.author.date,
                        DateTimeFormatter.ISO_DATE_TIME
                    ).toLocalDate()
                    allCommitDates.add(commitDate)
                }
            }

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

        } catch (e: Exception) {
            println("GitHub 통계 수집 중 에러 발생: ${e.message}")
            e.printStackTrace()
            throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
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

    fun getGithubAccountRanking(githubAccountId: Long): Int {
        val githubAccount = githubAccountRepository.findById(githubAccountId).orElseThrow {
            CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
        }

        val stats = githubStatsRepository.findByGithubAccount(githubAccount)
            ?: throw CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)

        val ranking = githubStatsRepository.countByTotalCommitsGreaterThan(stats.totalCommits)
        return (ranking + 1).toInt()
    }
}
