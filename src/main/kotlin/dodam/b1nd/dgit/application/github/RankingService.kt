package dodam.b1nd.dgit.application.github

import dodam.b1nd.dgit.presentation.github.dto.response.*
import dodam.b1nd.dgit.domain.github.repository.repository.RepositoryRepository
import dodam.b1nd.dgit.domain.github.stats.repository.GithubStatsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class RankingService(
    private val githubStatsRepository: GithubStatsRepository,
    private val repositoryRepository: RepositoryRepository
) {

    fun getUserTotalRanking(): List<UserTotalRankingResponse> {
        val allStats = githubStatsRepository.findAll()

        return allStats
            .sortedByDescending { it.level + it.totalCommits }
            .mapIndexed { index, stats ->
                UserTotalRankingResponse(
                    rank = index + 1,
                    level = stats.level,
                    totalCommits = stats.totalCommits,
                    avatarUrl = stats.githubAccount.avatarUrl,
                    username = stats.githubAccount.username,
                    name = stats.githubAccount.name,
                    bio = stats.githubAccount.bio
                )
            }
    }

    fun getUserCommitRanking(): List<UserCommitRankingResponse> {
        val allStats = githubStatsRepository.findAll()

        return allStats
            .sortedByDescending { it.totalCommits }
            .mapIndexed { index, stats ->
                UserCommitRankingResponse(
                    rank = index + 1,
                    totalCommits = stats.totalCommits,
                    avatarUrl = stats.githubAccount.avatarUrl,
                    username = stats.githubAccount.username,
                    name = stats.githubAccount.name,
                    bio = stats.githubAccount.bio
                )
            }
    }

    fun getRepositoryRanking(): List<RepositoryRankingResponse> {
        val approvedRepos = repositoryRepository.findAllApprovedOrderByTotalCommitsDesc()

        return approvedRepos.mapIndexed { index, repo ->
            RepositoryRankingResponse(
                rank = index + 1,
                totalCommits = repo.totalCommits,
                stars = repo.stars,
                ownerAvatarUrl = repo.ownerAvatarUrl,
                owner = repo.owner,
                repoName = repo.repoName
            )
        }
    }

    fun getStreakRanking(): List<StreakRankingResponse> {
        val allStats = githubStatsRepository.findAll()

        return allStats
            .sortedByDescending { it.longestStreak }
            .mapIndexed { index, stats ->
                StreakRankingResponse(
                    rank = index + 1,
                    longestStreak = stats.longestStreak,
                    avatarUrl = stats.githubAccount.avatarUrl,
                    username = stats.githubAccount.username,
                    name = stats.githubAccount.name,
                    bio = stats.githubAccount.bio
                )
            }
    }
}
