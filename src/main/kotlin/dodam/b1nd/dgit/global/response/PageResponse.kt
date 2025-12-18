package dodam.b1nd.dgit.global.response

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@Schema(description = "페이지네이션 응답")
data class PageResponse<T>(
    @Schema(description = "데이터 목록")
    val content: List<T>,

    @Schema(description = "전체 데이터 개수", example = "100")
    val totalElements: Long,

    @Schema(description = "전체 페이지 수", example = "5")
    val totalPages: Int,

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    val currentPage: Int,

    @Schema(description = "페이지 크기", example = "20")
    val size: Int,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean,

    @Schema(description = "이전 페이지 존재 여부", example = "false")
    val hasPrevious: Boolean
) {
    companion object {
        fun <T> from(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = page.number,
                size = page.size,
                hasNext = page.hasNext(),
                hasPrevious = page.hasPrevious()
            )
        }
    }
}
