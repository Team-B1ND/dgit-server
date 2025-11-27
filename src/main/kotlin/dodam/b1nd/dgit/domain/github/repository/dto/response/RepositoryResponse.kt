package dodam.b1nd.dgit.domain.github.repository.dto.response

import dodam.b1nd.dgit.domain.github.repository.entity.Repository
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "레포지토리 응답")
data class RepositoryResponse(
    @Schema(description = "레포지토리 ID")
    val id: Long,

    @Schema(description = "소유자", example = "octocat")
    val owner: String,

    @Schema(description = "레포지토리 이름", example = "Hello-World")
    val repoName: String,

    @Schema(description = "승인 여부")
    val isApproved: Boolean,

    @Schema(description = "전체 커밋 수")
    val totalCommits: Int,

    @Schema(description = "스타 수")
    val stars: Int,

    @Schema(description = "등록 일시")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(repository: Repository): RepositoryResponse {
            return RepositoryResponse(
                id = repository.id!!,
                owner = repository.owner,
                repoName = repository.repoName,
                isApproved = repository.isApproved,
                totalCommits = repository.totalCommits,
                stars = repository.stars,
                createdAt = repository.createdAt
            )
        }
    }
}
