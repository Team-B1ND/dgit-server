package dodam.b1nd.dgit.domain.github.fame.controller.docs

import dodam.b1nd.dgit.domain.github.fame.dto.response.HallOfFameResponse
import dodam.b1nd.dgit.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Hall of Fame", description = "명예의 전당 API")
interface HallOfFameControllerDocs {

    @Operation(
        summary = "명예의 전당 조회",
        description = "역대 주간 최다 커밋 기록 랭킹을 조회합니다."
    )
    fun getHallOfFame(): ApiResponse<List<HallOfFameResponse>>
}
