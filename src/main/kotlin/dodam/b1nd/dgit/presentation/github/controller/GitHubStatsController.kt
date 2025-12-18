package dodam.b1nd.dgit.presentation.github.controller

import dodam.b1nd.dgit.application.github.GithubStatsService
import dodam.b1nd.dgit.presentation.common.ApiResponse
import dodam.b1nd.dgit.presentation.github.controller.docs.GitHubStatsControllerDocs
import dodam.b1nd.dgit.presentation.github.dto.response.GithubStatsResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stats")
class GitHubStatsController(
    private val githubStatsService: GithubStatsService
) : GitHubStatsControllerDocs {

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/{githubAccountId}")
    override fun getGithubAccountStats(
        @PathVariable githubAccountId: Long
    ): ApiResponse<GithubStatsResponse> {
        val stats = githubStatsService.getStats(githubAccountId)
        val ranking = githubStatsService.getAccountRanking(githubAccountId)

        val response = GithubStatsResponse.from(stats, ranking)

        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "Github 통계 조회 성공",
            data = response
        )
    }
}
