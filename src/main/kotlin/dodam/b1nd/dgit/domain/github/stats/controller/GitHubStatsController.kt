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
        description = "특정 Github 계정의 통계를 조회합니다. 매시간 정각에 일괄 업데이트된 캐시 데이터를 반환합니다.",
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
