package dodam.b1nd.dgit.domain.github.stats.dto.response

import dodam.b1nd.dgit.domain.github.stats.entity.GithubStats
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Github 통계 응답")
data class GithubStatsResponse(
    @Schema(description = "Github 사용자 아이디", example = "octocat")
    val username: String,

    @Schema(description = "Github 사용자 이름", example = "The Octocat")
    val name: String?,

    @Schema(description = "Github 사용자 소개", example = "Developer from Seoul")
    val bio: String?,

    @Schema(description = "오늘의 커밋 수", example = "5")
    val todayCommits: Int,

    @Schema(description = "이번 주 커밋 수", example = "23")
    val weekCommits: Int,

    @Schema(description = "총 커밋 수", example = "1234")
    val totalCommits: Int,

    @Schema(description = "리포지토리 개수", example = "15")
    val repositoryCount: Int,

    @Schema(description = "최장 스트릭 (연속 커밋 일수)", example = "30")
    val longestStreak: Int,

    @Schema(description = "현재 스트릭 (연속 커밋 일수)", example = "7")
    val currentStreak: Int,

    @Schema(description = "랭킹 (전체 사용자 중 순위)", example = "3")
    val ranking: Int,

    @Schema(description = "레벨", example = "1")
    val level: Int,

    @Schema(description = "마지막 업데이트 시간")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(stats: GithubStats, ranking: Int): GithubStatsResponse {
            return GithubStatsResponse(
                username = stats.githubAccount.username,
                name = stats.githubAccount.name,
                bio = stats.githubAccount.bio,
                todayCommits = stats.todayCommits,
                weekCommits = stats.weekCommits,
                totalCommits = stats.totalCommits,
                repositoryCount = stats.repositoryCount,
                longestStreak = stats.longestStreak,
                currentStreak = stats.currentStreak,
                ranking = ranking,
                level = stats.level,
                updatedAt = stats.updatedAt
            )
        }
    }
}
