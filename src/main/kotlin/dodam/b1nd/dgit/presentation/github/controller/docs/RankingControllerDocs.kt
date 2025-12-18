package dodam.b1nd.dgit.presentation.github.controller.docs

import dodam.b1nd.dgit.presentation.github.dto.response.*
import dodam.b1nd.dgit.presentation.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Ranking", description = "랭킹 API")
interface RankingControllerDocs {

    @Operation(
        summary = "사용자 통합 랭킹 조회",
        description = "레벨과 커밋 수를 종합한 사용자 랭킹을 조회합니다."
    )
    fun getUserTotalRanking(): ApiResponse<List<UserTotalRankingResponse>>

    @Operation(
        summary = "사용자 커밋 랭킹 조회",
        description = "커밋 수 기준 사용자 랭킹을 조회합니다."
    )
    fun getUserCommitRanking(): ApiResponse<List<UserCommitRankingResponse>>

    @Operation(
        summary = "레포지토리 랭킹 조회",
        description = "승인된 레포지토리의 커밋 수 기준 랭킹을 조회합니다."
    )
    fun getRepositoryRanking(): ApiResponse<List<RepositoryRankingResponse>>

    @Operation(
        summary = "최장 스트릭 랭킹 조회",
        description = "최장 스트릭 기준 사용자 랭킹을 조회합니다."
    )
    fun getStreakRanking(): ApiResponse<List<StreakRankingResponse>>
}
