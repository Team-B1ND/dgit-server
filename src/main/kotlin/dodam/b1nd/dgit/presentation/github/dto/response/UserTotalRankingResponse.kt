package dodam.b1nd.dgit.presentation.github.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 통합 랭킹 응답")
data class UserTotalRankingResponse(
    @Schema(description = "등수", example = "1")
    val rank: Int,

    @Schema(description = "레벨", example = "1")
    val level: Int,

    @Schema(description = "총 커밋 수", example = "1234")
    val totalCommits: Int,

    @Schema(description = "프로필 이미지 URL")
    val avatarUrl: String?,

    @Schema(description = "Github 아이디", example = "octocat")
    val username: String,

    @Schema(description = "Github 사용자 이름", example = "The Octocat")
    val name: String?,

    @Schema(description = "Github 사용자 소개")
    val bio: String?
)
