package dodam.b1nd.dgit.domain.github.ranking.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "레포지토리 랭킹 응답")
data class RepositoryRankingResponse(
    @Schema(description = "등수", example = "1")
    val rank: Int,

    @Schema(description = "전체 커밋 수", example = "5678")
    val totalCommits: Int,

    @Schema(description = "스타 수", example = "1234")
    val stars: Int,

    @Schema(description = "소유자/조직 프로필 이미지 URL")
    val ownerAvatarUrl: String?,

    @Schema(description = "Github 아이디 또는 조직 아이디", example = "octocat")
    val owner: String,

    @Schema(description = "레포지토리 이름", example = "Hello-World")
    val repoName: String
)
