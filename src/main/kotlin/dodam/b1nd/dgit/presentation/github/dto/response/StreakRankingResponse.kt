package dodam.b1nd.dgit.presentation.github.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "최장 스트릭 랭킹 응답")
data class StreakRankingResponse(
    @Schema(description = "등수", example = "1")
    val rank: Int,

    @Schema(description = "최장 스트릭 (일 수)", example = "365")
    val longestStreak: Int,

    @Schema(description = "프로필 이미지 URL")
    val avatarUrl: String?,

    @Schema(description = "Github 아이디", example = "octocat")
    val username: String,

    @Schema(description = "Github 사용자 이름", example = "The Octocat")
    val name: String?,

    @Schema(description = "Github 사용자 소개")
    val bio: String?
)
