package dodam.b1nd.dgit.domain.github.stats.controller

import dodam.b1nd.dgit.domain.github.stats.dto.response.GithubStatsResponse
import dodam.b1nd.dgit.domain.github.stats.service.GithubStatsService
import dodam.b1nd.dgit.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Github Stats", description = "Github 통계 API")
@RestController
@RequestMapping("/stats")
class GitHubStatsController(
    private val githubStatsService: GithubStatsService
) {

    @Operation(
        summary = "Github 계정 통계 조회",
        description = """
            특정 Github 계정의 통계를 조회합니다.

            **조회 가능한 통계:**
            - 전체 커밋 수 (totalCommits)
            - 오늘 커밋 수 (todayCommits)
            - 이번 주 커밋 수 (weekCommits)
            - 레포지토리 개수 (repositoryCount)
            - 최장 스트릭 (longestStreak)
            - 현재 스트릭 (currentStreak)
            - 커밋 랭킹 (ranking)

            **데이터 갱신:**
            - GraphQL API를 통해 커밋 날짜만 수집하여 효율적으로 처리
            - 매시간(07:00-23:00) 자동으로 데이터가 갱신됩니다.
            - 최신 데이터가 아닐 수 있으니, 실시간 반영이 필요한 경우 대기 후 재조회해주세요.
        """.trimIndent(),
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/{githubAccountId}")
    fun getGithubAccountStats(
        @PathVariable githubAccountId: Long
    ): ApiResponse<GithubStatsResponse> {
        val stats = githubStatsService.getStats(githubAccountId)
        val ranking = githubStatsService.getGithubAccountRanking(githubAccountId)

        val response = GithubStatsResponse.from(stats, ranking)

        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "Github 통계 조회 성공",
            data = response
        )
    }
}
