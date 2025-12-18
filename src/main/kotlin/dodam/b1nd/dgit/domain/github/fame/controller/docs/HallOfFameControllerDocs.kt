package dodam.b1nd.dgit.domain.github.fame.controller.docs

import dodam.b1nd.dgit.domain.github.fame.dto.response.FirstPlaceRankingResponse
import dodam.b1nd.dgit.domain.github.fame.dto.response.HallOfFameResponse
import dodam.b1nd.dgit.global.response.ApiResponse
import dodam.b1nd.dgit.global.response.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable

@Tag(name = "Hall of Fame", description = "명예의 전당 API")
interface HallOfFameControllerDocs {

    @Operation(
        summary = "명예의 전당 조회",
        description = "역대 주간 최다 커밋 기록 랭킹을 페이지네이션으로 조회합니다. 일요일부터 토요일까지 한 주간의 커밋 개수로 랭킹을 매깁니다."
    )
    fun getHallOfFame(
        @Parameter(description = "페이지네이션 정보 (page, size)")
        pageable: Pageable
    ): ApiResponse<PageResponse<HallOfFameResponse>>

    @Operation(
        summary = "주간 1등 횟수 랭킹 조회",
        description = "사용자별로 주간 커밋 랭킹에서 1등을 얼마나 많이 했는지 조회합니다."
    )
    fun getFirstPlaceRankings(): ApiResponse<List<FirstPlaceRankingResponse>>
}
