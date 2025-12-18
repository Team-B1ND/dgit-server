package dodam.b1nd.dgit.infrastructure.client

import dodam.b1nd.dgit.presentation.github.dto.external.GithubCommit
import dodam.b1nd.dgit.presentation.github.dto.external.GithubRepository
import dodam.b1nd.dgit.presentation.github.dto.external.GithubUser
import dodam.b1nd.dgit.infrastructure.config.GithubProperties
import dodam.b1nd.dgit.infrastructure.exception.CustomException
import dodam.b1nd.dgit.infrastructure.exception.ErrorCode
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class GithubClient(
    private val webClientBuilder: WebClient.Builder,
    private val githubProperties: GithubProperties
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
            .header("Authorization", "token ${githubProperties.token}")
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
                .header("Authorization", "token ${githubProperties.token}")
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
     * GraphQL로 커밋 날짜만 조회 (응답 크기 99% 감소)
     */
    fun getCommitDates(
        owner: String,
        repo: String,
        author: String
    ): List<LocalDate> {
        val webClient = webClientBuilder
            .baseUrl(GITHUB_API_BASE_URL)
            .build()

        val allDates = mutableListOf<LocalDate>()
        var hasNextPage = true
        var endCursor: String? = null

        while (hasNextPage) {
            val query = buildGraphQLQuery(owner, repo, author, endCursor)
            val requestBody = mapOf("query" to query)

            val response = webClient.post()
                .uri("/graphql")
                .header("Authorization", "Bearer ${githubProperties.token}")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono<Map<String, Any>>()
                .block() ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

            val dates = parseGraphQLResponse(response)
            allDates.addAll(dates)

            val pageInfo = extractPageInfo(response)
            hasNextPage = pageInfo["hasNextPage"] as? Boolean ?: false
            endCursor = pageInfo["endCursor"] as? String
        }

        return allDates
    }

    private fun buildGraphQLQuery(owner: String, repo: String, author: String, cursor: String?): String {
        val afterClause = if (cursor != null) """, after: "$cursor"""" else ""
        return """
            {
              repository(owner: "$owner", name: "$repo") {
                defaultBranchRef {
                  target {
                    ... on Commit {
                      history(first: 100, author: { id: "$author" }$afterClause) {
                        pageInfo {
                          hasNextPage
                          endCursor
                        }
                        nodes {
                          committedDate
                        }
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()
    }

    private fun parseGraphQLResponse(response: Map<String, Any>): List<LocalDate> {
        try {
            val data = response["data"] as? Map<*, *> ?: return emptyList()
            val repository = data["repository"] as? Map<*, *> ?: return emptyList()
            val defaultBranchRef = repository["defaultBranchRef"] as? Map<*, *> ?: return emptyList()
            val target = defaultBranchRef["target"] as? Map<*, *> ?: return emptyList()
            val history = target["history"] as? Map<*, *> ?: return emptyList()
            val nodes = history["nodes"] as? List<*> ?: return emptyList()

            return nodes.mapNotNull { node ->
                val nodeMap = node as? Map<*, *>
                val dateString = nodeMap?.get("committedDate") as? String
                dateString?.let {
                    LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
                }
            }
        } catch (e: Exception) {
            println("GraphQL 응답 파싱 실패: ${e.message}")
            return emptyList()
        }
    }

    private fun extractPageInfo(response: Map<String, Any>): Map<String, Any> {
        try {
            val data = response["data"] as? Map<*, *> ?: return emptyMap()
            val repository = data["repository"] as? Map<*, *> ?: return emptyMap()
            val defaultBranchRef = repository["defaultBranchRef"] as? Map<*, *> ?: return emptyMap()
            val target = defaultBranchRef["target"] as? Map<*, *> ?: return emptyMap()
            val history = target["history"] as? Map<*, *> ?: return emptyMap()
            val pageInfo = history["pageInfo"] as? Map<*, *> ?: return emptyMap()

            return pageInfo.mapKeys { it.key.toString() }.mapValues { it.value ?: false }
        } catch (e: Exception) {
            return emptyMap()
        }
    }

    /**
     * 특정 리포지토리의 커밋 조회 (특정 author, since 날짜 이후)
     * @deprecated GraphQL 버전(getCommitDates)을 사용하세요
     */
    @Deprecated("Use getCommitDates instead", ReplaceWith("getCommitDates(owner, repo, author)"))
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
                .header("Authorization", "token ${githubProperties.token}")
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
