package dodam.b1nd.dgit.domain.github.ranking.controller

import dodam.b1nd.dgit.domain.github.ranking.dto.response.*
import dodam.b1nd.dgit.domain.github.ranking.service.RankingService
import dodam.b1nd.dgit.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Ranking", description = "랭킹 API")
@RestController
@RequestMapping("/ranking")
class RankingController(
    private val rankingService: RankingService
) {

    @Operation(summary = "사용자 통합 랭킹 조회", description = "레벨 + 커밋 수 기준 랭킹")
    @GetMapping("/total")
    fun getUserTotalRanking(): ApiResponse<List<UserTotalRankingResponse>> {
        val ranking = rankingService.getUserTotalRanking()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "사용자 통합 랭킹 조회 성공",
            data = ranking
        )
    }

    @Operation(summary = "사용자 커밋 랭킹 조회", description = "커밋 수 기준 랭킹")
    @GetMapping("/commit")
    fun getUserCommitRanking(): ApiResponse<List<UserCommitRankingResponse>> {
        val ranking = rankingService.getUserCommitRanking()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "사용자 커밋 랭킹 조회 성공",
            data = ranking
        )
    }

    @Operation(summary = "레포지토리 랭킹 조회", description = "레포지토리 커밋 수 기준 랭킹 (승인된 레포지토리만)")
    @GetMapping("/repository")
    fun getRepositoryRanking(): ApiResponse<List<RepositoryRankingResponse>> {
        val ranking = rankingService.getRepositoryRanking()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "레포지토리 랭킹 조회 성공",
            data = ranking
        )
    }

    @Operation(summary = "최장 스트릭 랭킹 조회", description = "최장 스트릭 기준 랭킹")
    @GetMapping("/streak")
    fun getStreakRanking(): ApiResponse<List<StreakRankingResponse>> {
        val ranking = rankingService.getStreakRanking()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "최장 스트릭 랭킹 조회 성공",
            data = ranking
        )
    }
}
