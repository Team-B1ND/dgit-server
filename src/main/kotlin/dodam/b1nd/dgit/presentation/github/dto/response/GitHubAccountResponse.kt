package dodam.b1nd.dgit.presentation.github.dto.response

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Github 계정 응답")
data class GithubAccountResponse(
    @Schema(description = "Github 계정 ID")
    val id: Long,

    @Schema(description = "Github 사용자 아이디", example = "octocat")
    val username: String,

    @Schema(description = "등록 일시")
    val createdAt: LocalDateTime,

    @Schema(description = "수정 일시")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(githubAccount: GithubAccount): GithubAccountResponse {
            return GithubAccountResponse(
                id = githubAccount.id!!,
                username = githubAccount.username,
                createdAt = githubAccount.createdAt,
                updatedAt = githubAccount.updatedAt
            )
        }
    }
}
