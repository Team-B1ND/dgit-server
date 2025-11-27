package dodam.b1nd.dgit.domain.github.fame.controller

import dodam.b1nd.dgit.domain.github.fame.dto.response.HallOfFameResponse
import dodam.b1nd.dgit.domain.github.fame.service.HallOfFameService
import dodam.b1nd.dgit.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Hall of Fame", description = "명예의 전당 API")
@RestController
@RequestMapping("/hall-of-fame")
class HallOfFameController(
    private val hallOfFameService: HallOfFameService
) {

    @Operation(summary = "명예의 전당 조회", description = "역대 주간 최다 커밋 기록 랭킹")
    @GetMapping
    fun getHallOfFame(): ApiResponse<List<HallOfFameResponse>> {
        val hallOfFame = hallOfFameService.getHallOfFame()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "명예의 전당 조회 성공",
            data = hallOfFame
        )
    }
}
