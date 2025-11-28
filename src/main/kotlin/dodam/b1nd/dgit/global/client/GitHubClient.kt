package dodam.b1nd.dgit.global.client

import dodam.b1nd.dgit.domain.github.stats.dto.external.GithubCommit
import dodam.b1nd.dgit.domain.github.stats.dto.external.GithubRepository
import dodam.b1nd.dgit.domain.github.stats.dto.external.GithubUser
import dodam.b1nd.dgit.global.exception.CustomException
import dodam.b1nd.dgit.global.exception.ErrorCode
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class GithubClient(
    private val webClientBuilder: WebClient.Builder
) {

    companion object {
        private const val GITHUB_API_BASE_URL = "https://api.github.com"
        private const val PER_PAGE = 100
    }

    fun getUser(username: String): GithubUser {
        val webClient = webClientBuilder
            .baseUrl(GITHUB_API_BASE_URL)
            .build()

        return webClient.get()
            .uri("/users/$username")
            .retrieve()
            .bodyToMono<GithubUser>()
            .block() ?: throw CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
    }

    /**
     * 사용자의 모든 public 리포지토리 조회
     */
    fun getUserRepositories(username: String): List<GithubRepository> {
        val webClient = webClientBuilder
            .baseUrl(GITHUB_API_BASE_URL)
            .build()

        val repositories = mutableListOf<GithubRepository>()
        var page = 1
        var hasMore = true

        while (hasMore) {
            val response = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/users/$username/repos")
                        .queryParam("type", "owner")
                        .queryParam("per_page", PER_PAGE)
                        .queryParam("page", page)
                        .build()
                }
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<GithubRepository>>() {})
                .block() ?: emptyList()

            repositories.addAll(response)
            hasMore = response.size == PER_PAGE
            page++
        }

        return repositories
    }

    /**
     * 특정 리포지토리의 커밋 조회 (특정 author, since 날짜 이후)
     */
    fun getRepositoryCommits(
        owner: String,
        repo: String,
        author: String,
        since: LocalDate? = null
    ): List<GithubCommit> {
        val webClient = webClientBuilder
            .baseUrl(GITHUB_API_BASE_URL)
            .build()

        val commits = mutableListOf<GithubCommit>()
        var page = 1
        var hasMore = true

        while (hasMore) {
            val response = webClient.get()
                .uri { uriBuilder ->
                    val builder = uriBuilder
                        .path("/repos/$owner/$repo/commits")
                        .queryParam("author", author)
                        .queryParam("per_page", PER_PAGE)
                        .queryParam("page", page)

                    if (since != null) {
                        builder.queryParam("since", since.atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME))
                    }

                    builder.build()
                }
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<GithubCommit>>() {})
                .block() ?: emptyList()

            commits.addAll(response)
            hasMore = response.size == PER_PAGE
            page++
        }

        return commits
    }
}
