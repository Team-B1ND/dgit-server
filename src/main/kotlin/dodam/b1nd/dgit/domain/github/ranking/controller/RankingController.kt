package dodam.b1nd.dgit.domain.github.ranking.controller

import dodam.b1nd.dgit.domain.github.ranking.controller.docs.RankingControllerDocs
import dodam.b1nd.dgit.domain.github.ranking.dto.response.*
import dodam.b1nd.dgit.domain.github.ranking.service.RankingService
import dodam.b1nd.dgit.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/ranking")
class RankingController(
    private val rankingService: RankingService
) : RankingControllerDocs {

    @GetMapping("/total")
    override fun getUserTotalRanking(): ApiResponse<List<UserTotalRankingResponse>> {
        val ranking = rankingService.getUserTotalRanking()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "사용자 통합 랭킹 조회 성공",
            data = ranking
        )
    }

    @GetMapping("/commit")
    override fun getUserCommitRanking(): ApiResponse<List<UserCommitRankingResponse>> {
        val ranking = rankingService.getUserCommitRanking()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "사용자 커밋 랭킹 조회 성공",
            data = ranking
        )
    }

    @GetMapping("/repository")
    override fun getRepositoryRanking(): ApiResponse<List<RepositoryRankingResponse>> {
        val ranking = rankingService.getRepositoryRanking()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "레포지토리 랭킹 조회 성공",
            data = ranking
        )
    }

    @GetMapping("/streak")
    override fun getStreakRanking(): ApiResponse<List<StreakRankingResponse>> {
        val ranking = rankingService.getStreakRanking()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "최장 스트릭 랭킹 조회 성공",
            data = ranking
        )
    }
}
