package dodam.b1nd.dgit.presentation.github.controller

import dodam.b1nd.dgit.application.github.GithubStatsService
import dodam.b1nd.dgit.application.github.usecase.GithubStatsUseCase
import dodam.b1nd.dgit.presentation.common.ApiResponse
import dodam.b1nd.dgit.presentation.github.controller.docs.GithubStatsControllerDocs
import dodam.b1nd.dgit.presentation.github.dto.response.GithubStatsResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stats")
class GithubStatsController(
    private val githubStatsService: GithubStatsService,
    private val githubStatsUseCase: GithubStatsUseCase
) : GithubStatsControllerDocs {

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/{githubAccountId}")
    override fun getGithubAccountStats(
        @PathVariable githubAccountId: Long
    ): ApiResponse<GithubStatsResponse> {
        val stats = githubStatsService.getStats(githubAccountId)
        val ranking = githubStatsService.getAccountRanking(githubAccountId)
        val percentile = githubStatsService.getAccountPercentile(githubAccountId)

        val response = GithubStatsResponse.from(stats, ranking, percentile)

        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "Github 통계 조회 성공",
            data = response
        )
    }

    @PostMapping("/update-all")
    override fun updateAllStats(): ApiResponse<Unit> {
        githubStatsUseCase.updateAllStatsScheduled()

        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "전체 사용자 통계 업데이트 완료",
            data = Unit
        )
    }
}
