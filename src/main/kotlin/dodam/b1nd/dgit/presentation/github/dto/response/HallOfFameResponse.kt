package dodam.b1nd.dgit.presentation.github.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "명예의 전당 응답")
data class HallOfFameResponse(
    @Schema(description = "등수", example = "1")
    val rank: Int,

    @Schema(description = "주간 시작일 (일요일)", example = "2025-01-12")
    val weekStart: LocalDate,

    @Schema(description = "주간 커밋 수", example = "150")
    val weekCommits: Int,

    @Schema(description = "프로필 이미지 URL")
    val avatarUrl: String?,

    @Schema(description = "Github 아이디", example = "octocat")
    val githubName: String,

    @Schema(description = "Github 사용자 이름", example = "The Octocat")
    val name: String?,

    @Schema(description = "Github 사용자 소개")
    val bio: String?,

    @Schema(description = "1등 횟수", example = "3")
    val firstPlaceCount: Int
)
