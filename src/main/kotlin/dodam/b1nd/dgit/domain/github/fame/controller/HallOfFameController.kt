package dodam.b1nd.dgit.domain.github.fame.controller

import dodam.b1nd.dgit.domain.github.fame.controller.docs.HallOfFameControllerDocs
import dodam.b1nd.dgit.domain.github.fame.dto.response.FirstPlaceRankingResponse
import dodam.b1nd.dgit.domain.github.fame.dto.response.HallOfFameResponse
import dodam.b1nd.dgit.domain.github.fame.service.HallOfFameService
import dodam.b1nd.dgit.global.response.ApiResponse
import dodam.b1nd.dgit.global.response.PageResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/hall-of-fame")
class HallOfFameController(
    private val hallOfFameService: HallOfFameService
) : HallOfFameControllerDocs {

    @GetMapping
    override fun getHallOfFame(
        @PageableDefault(size = 20)
        pageable: Pageable
    ): ApiResponse<PageResponse<HallOfFameResponse>> {
        val hallOfFame = hallOfFameService.getHallOfFame(pageable)
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "명예의 전당 조회 성공",
            data = hallOfFame
        )
    }

    @GetMapping("/first-place")
    override fun getFirstPlaceRankings(): ApiResponse<List<FirstPlaceRankingResponse>> {
        val rankings = hallOfFameService.getFirstPlaceRankings()
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "주간 1등 횟수 랭킹 조회 성공",
            data = rankings
        )
    }
}
