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

    fun getAllUserCommitDates(username: String): List<LocalDate> {
        val allDates = mutableListOf<LocalDate>()
        val currentYear = LocalDate.now().year
        val startYear = 2008

        for (year in startYear..currentYear) {
            val from = "${year}-01-01T00:00:00Z"
            val to = "${year}-12-31T23:59:59Z"
            val dates = getUserCommitDatesForYear(username, from, to)
            allDates.addAll(dates)
        }

        return allDates
    }

    private fun getUserCommitDatesForYear(username: String, from: String, to: String): List<LocalDate> {
        val webClient = webClientBuilder
            .baseUrl(GITHUB_API_BASE_URL)
            .build()

        val query = """
            {
              user(login: "$username") {
                contributionsCollection(from: "$from", to: "$to") {
                  commitContributionsByRepository {
                    contributions {
                      nodes {
                        occurredAt
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val requestBody = mapOf("query" to query)

        val response = webClient.post()
            .uri("/graphql")
            .header("Authorization", "Bearer ${githubProperties.token}")
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono<Map<String, Any>>()
            .block() ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

        return parseContributionsResponse(response)
    }

    private fun parseContributionsResponse(response: Map<String, Any>): List<LocalDate> {
        try {
            val data = response["data"] as? Map<*, *> ?: return emptyList()
            val user = data["user"] as? Map<*, *> ?: return emptyList()
            val contributionsCollection = user["contributionsCollection"] as? Map<*, *> ?: return emptyList()
            val commitContributionsByRepository = contributionsCollection["commitContributionsByRepository"] as? List<*> ?: return emptyList()

            val dates = mutableListOf<LocalDate>()

            for (repoContribution in commitContributionsByRepository) {
                val repoMap = repoContribution as? Map<*, *> ?: continue
                val contributions = repoMap["contributions"] as? Map<*, *> ?: continue
                val nodes = contributions["nodes"] as? List<*> ?: continue

                for (node in nodes) {
                    val nodeMap = node as? Map<*, *> ?: continue
                    val occurredAt = nodeMap["occurredAt"] as? String ?: continue
                    dates.add(LocalDateTime.parse(occurredAt, DateTimeFormatter.ISO_DATE_TIME).toLocalDate())
                }
            }

            return dates
        } catch (e: Exception) {
            return emptyList()
        }
    }

    @Deprecated("Use getAllUserCommitDates instead", ReplaceWith("getAllUserCommitDates(author)"))
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

            val dates = parseGraphQLResponse(response, author)
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
                      history(first: 100$afterClause) {
                        pageInfo {
                          hasNextPage
                          endCursor
                        }
                        nodes {
                          committedDate
                          author {
                            user {
                              login
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()
    }

    private fun parseGraphQLResponse(response: Map<String, Any>, authorUsername: String): List<LocalDate> {
        try {
            val data = response["data"] as? Map<*, *> ?: return emptyList()
            val repository = data["repository"] as? Map<*, *> ?: return emptyList()
            val defaultBranchRef = repository["defaultBranchRef"] as? Map<*, *> ?: return emptyList()
            val target = defaultBranchRef["target"] as? Map<*, *> ?: return emptyList()
            val history = target["history"] as? Map<*, *> ?: return emptyList()
            val nodes = history["nodes"] as? List<*> ?: return emptyList()

            return nodes.mapNotNull { node ->
                val nodeMap = node as? Map<*, *> ?: return@mapNotNull null
                val dateString = nodeMap["committedDate"] as? String
                val authorMap = nodeMap["author"] as? Map<*, *>
                val userMap = authorMap?.get("user") as? Map<*, *>
                val login = userMap?.get("login") as? String

                if (login?.equals(authorUsername, ignoreCase = true) == true && dateString != null) {
                    LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
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
