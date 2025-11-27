package dodam.b1nd.dgit.domain.github.stats.dto.external

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubRepository(
    val id: Long,
    val name: String,
    @JsonProperty("full_name")
    val fullName: String,
    val owner: GithubOwner,
    val private: Boolean,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("updated_at")
    val updatedAt: String
)

data class GithubOwner(
    val login: String,
    val id: Long,
    @JsonProperty("avatar_url")
    val avatarUrl: String
)

data class GithubCommit(
    val sha: String,
    val commit: GithubCommitDetail,
    val author: GithubAuthor?
)

data class GithubCommitDetail(
    val author: GithubCommitAuthor,
    val message: String
)

data class GithubCommitAuthor(
    val name: String,
    val email: String,
    val date: String
)

data class GithubAuthor(
    val login: String,
    val id: Long
)

data class GithubUser(
    val login: String,
    val id: Long,
    val name: String?,
    val bio: String?,
    @JsonProperty("avatar_url")
    val avatarUrl: String
)
