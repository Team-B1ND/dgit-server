package dodam.b1nd.dgit.presentation.github.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "1등 횟수 랭킹 응답")
data class FirstPlaceRankingResponse(
    @Schema(description = "Github 아이디", example = "octocat")
    val githubName: String,

    @Schema(description = "Github 사용자 이름", example = "The Octocat")
    val name: String?,

    @Schema(description = "프로필 이미지 URL")
    val avatarUrl: String?,

    @Schema(description = "주간 1등 횟수", example = "5")
    val firstPlaceCount: Int
)
