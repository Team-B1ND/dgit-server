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

data class UserCommitData(
    val totalCommits: Int,
    val commitDates: List<LocalDate>
)

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

    fun getAllUserCommitDates(username: String): UserCommitData {
        return getAllUserCommitDatesFromSearch(username)
    }

    private fun getAllUserCommitDatesFromSearch(username: String): UserCommitData {
        val allDates = mutableListOf<LocalDate>()
        var totalCommitCount = 0
        val currentYear = LocalDate.now().year
        val startYear = 2008

        println("🔍 [Search API] Starting commit search for user: $username")

        for (year in startYear..currentYear) {
            for (month in 1..12) {
                if (year == currentYear && month > LocalDate.now().monthValue) {
                    break
                }

                val startDate = LocalDate.of(year, month, 1)
                val endDate = startDate.plusMonths(1).minusDays(1)

                val monthData = searchCommitsByMonth(username, startDate, endDate)
                allDates.addAll(monthData.commitDates)
                totalCommitCount += monthData.totalCommits

                if (monthData.totalCommits > 0) {
                    println("  📅 $year-${month.toString().padStart(2, '0')}: ${monthData.totalCommits} commits")
                }
            }
        }

        println("🎯 [Search API] Total commits found: $totalCommitCount")
        return UserCommitData(totalCommitCount, allDates.distinct())
    }

    private fun searchCommitsByMonth(username: String, startDate: LocalDate, endDate: LocalDate): UserCommitData {
        val webClient = webClientBuilder
            .baseUrl(GITHUB_API_BASE_URL)
            .build()

        val query = "author:$username+author-date:$startDate..$endDate"
        val allCommits = mutableListOf<Map<*, *>>()
        var page = 1
        val perPage = 100

        while (page <= 10) {
            val response = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/search/commits")
                        .queryParam("q", query)
                        .queryParam("per_page", perPage)
                        .queryParam("page", page)
                        .build()
                }
                .header("Authorization", "Bearer ${githubProperties.token}")
                .header("Accept", "application/vnd.github.cloak-preview")
                .retrieve()
                .bodyToMono<Map<String, Any>>()
                .block() ?: break

            val items = response["items"] as? List<*> ?: break
            if (items.isEmpty()) break

            allCommits.addAll(items.filterIsInstance<Map<*, *>>())

            val totalCount = response["total_count"] as? Int ?: 0
            if (allCommits.size >= totalCount || items.size < perPage) {
                break
            }

            page++
        }

        val dates = allCommits.mapNotNull { commit ->
            val commitData = commit["commit"] as? Map<*, *>
            val author = commitData?.get("author") as? Map<*, *>
            val dateStr = author?.get("date") as? String
            dateStr?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
            }
        }.distinct()

        return UserCommitData(allCommits.size, dates)
    }

    private fun getUserCommitDatesForYear(username: String, from: String, to: String): UserCommitData {
        val allDates = mutableListOf<LocalDate>()
        var totalCommitCount = 0
        var cursor: String? = null
        var hasNextPage = true
        var pageCount = 0

        while (hasNextPage && pageCount < 5) {
            pageCount++
            val data = fetchCommitPage(username, from, to, cursor)

            allDates.addAll(data.commitDates)
            totalCommitCount += data.totalCommits

            hasNextPage = false
            cursor = null
            break
        }

        return UserCommitData(totalCommitCount, allDates)
    }

    private fun fetchCommitPage(username: String, from: String, to: String, cursor: String?): UserCommitData {
        val webClient = webClientBuilder
            .baseUrl(GITHUB_API_BASE_URL)
            .build()

        val query = """
            {
              user(login: "$username") {
                contributionsCollection(from: "$from", to: "$to") {
                  commitContributionsByRepository(maxRepositories: 100) {
                    repository {
                      nameWithOwner
                    }
                    contributions(first: 100) {
                      totalCount
                      nodes {
                        occurredAt
                      }
                      pageInfo {
                        hasNextPage
                        endCursor
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

        return parseContributionsResponse(response, username)
    }

    private fun parseContributionsResponse(response: Map<String, Any>, username: String): UserCommitData {
        try {
            val errors = response["errors"] as? List<*>
            if (errors != null && errors.isNotEmpty()) {
                return UserCommitData(0, emptyList())
            }

            val data = response["data"] as? Map<*, *> ?: return UserCommitData(0, emptyList())
            val user = data["user"] as? Map<*, *> ?: return UserCommitData(0, emptyList())
            val contributionsCollection = user["contributionsCollection"] as? Map<*, *> ?: return UserCommitData(0, emptyList())
            val commitContributionsByRepository = contributionsCollection["commitContributionsByRepository"] as? List<*> ?: return UserCommitData(0, emptyList())

            val dates = mutableListOf<LocalDate>()
            var totalCommitsFromAPI = 0

            for (repoContribution in commitContributionsByRepository) {
                val repoMap = repoContribution as? Map<*, *> ?: continue
                val contributions = repoMap["contributions"] as? Map<*, *> ?: continue
                val totalCount = contributions["totalCount"] as? Int ?: 0
                val nodes = contributions["nodes"] as? List<*> ?: continue

                totalCommitsFromAPI += totalCount

                for (node in nodes) {
                    val nodeMap = node as? Map<*, *> ?: continue
                    val occurredAt = nodeMap["occurredAt"] as? String ?: continue
                    dates.add(LocalDateTime.parse(occurredAt, DateTimeFormatter.ISO_DATE_TIME).toLocalDate())
                }
            }

            return UserCommitData(totalCommitsFromAPI, dates)
        } catch (e: Exception) {
            return UserCommitData(0, emptyList())
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
