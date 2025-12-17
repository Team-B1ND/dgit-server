package dodam.b1nd.dgit.domain.github.fame.controller

import dodam.b1nd.dgit.domain.github.fame.controller.docs.HallOfFameControllerDocs
import dodam.b1nd.dgit.domain.github.fame.dto.response.HallOfFameResponse
import dodam.b1nd.dgit.domain.github.fame.service.HallOfFameService
import dodam.b1nd.dgit.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/hall-of-fame")
class HallOfFameController(
    private val hallOfFameService: HallOfFameService
) : HallOfFameControllerDocs {

    @GetMapping
    override fun getHallOfFame(): ApiResponse<List<HallOfFameResponse>> {
        val hallOfFame = hallOfFameService.getHallOfFame()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "명예의 전당 조회 성공",
            data = hallOfFame
        )
    }
}
