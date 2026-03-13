package dodam.b1nd.dgit.infrastructure.client

import dodam.b1nd.dgit.infrastructure.config.GithubProperties
import dodam.b1nd.dgit.infrastructure.exception.CustomException
import dodam.b1nd.dgit.infrastructure.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate

data class ContributionDay(
    val date: LocalDate,
    val count: Int
)

data class GithubUserData(
    val name: String?,
    val bio: String?,
    val avatarUrl: String,
    val createdAt: LocalDate,
    val repositoryCount: Int,
    var totalCommits: Int,
    val contributionDays: MutableList<ContributionDay>
)

@Component
class GithubClient(
    private val webClientBuilder: WebClient.Builder,
    private val githubProperties: GithubProperties
) {

    private val log = LoggerFactory.getLogger(GithubClient::class.java)

    companion object {
        private const val GITHUB_API_URL = "https://api.github.com"
        private const val MAX_YEARS_PER_QUERY = 6
    }

    fun fetchUserData(username: String): GithubUserData {
        val currentYear = LocalDate.now().year
        val recentYears = (maxOf(2008, currentYear - MAX_YEARS_PER_QUERY + 1)..currentYear).toList()

        log.info("Fetching user data for: {} (years: {}~{})", username, recentYears.first(), recentYears.last())

        val response = executeGraphQL(buildFullUserQuery(username, recentYears))
        val userData = parseFullUserResponse(response, recentYears)

        val createdYear = userData.createdAt.year
        val oldestFetched = recentYears.min()

        if (createdYear < oldestFetched) {
            val olderYears = (createdYear until oldestFetched).toList()
            for (batch in olderYears.chunked(MAX_YEARS_PER_QUERY)) {
                log.info("Fetching older contributions for: {} (years: {}~{})", username, batch.first(), batch.last())
                val olderResponse = executeGraphQL(buildContributionsOnlyQuery(username, batch))
                val (commits, days) = parseContributions(olderResponse, batch)
                userData.totalCommits += commits
                userData.contributionDays.addAll(days)
            }
        }

        log.info("Completed fetching data for: {} (totalCommits: {})", username, userData.totalCommits)
        return userData
    }

    fun fetchUserAvatarUrl(username: String): String {
        val query = """
            {
                user(login: "$username") {
                    avatarUrl
                }
            }
        """.trimIndent()

        val response = executeGraphQL(query)
        val user = extractUser(response)
        return user["avatarUrl"] as? String ?: throw CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
    }

    fun fetchContributionsForDateRange(username: String, from: LocalDate, to: LocalDate): List<ContributionDay> {
        val years = (from.year..to.year).toList()
        val response = executeGraphQL(buildContributionsOnlyQuery(username, years))
        val (_, days) = parseContributions(response, years)
        return days.filter { it.date in from..to }
    }

    fun fetchRepositoryStats(owner: String, repoName: String): Pair<Int, Int> {
        val query = """
            {
                repository(owner: "$owner", name: "$repoName") {
                    stargazerCount
                    defaultBranchRef {
                        target {
                            ... on Commit {
                                history {
                                    totalCount
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent()

        val response = executeGraphQL(query)
        val data = response["data"] as? Map<*, *> ?: return Pair(0, 0)
        val repository = data["repository"] as? Map<*, *> ?: return Pair(0, 0)

        val starCount = repository["stargazerCount"] as? Int ?: 0
        val defaultBranchRef = repository["defaultBranchRef"] as? Map<*, *>
        val target = defaultBranchRef?.get("target") as? Map<*, *>
        val history = target?.get("history") as? Map<*, *>
        val commitCount = history?.get("totalCount") as? Int ?: 0

        return Pair(commitCount, starCount)
    }

    private fun executeGraphQL(query: String): Map<String, Any> {
        val webClient = webClientBuilder.baseUrl(GITHUB_API_URL).build()

        val response = webClient.post()
            .uri("/graphql")
            .header("Authorization", "Bearer ${githubProperties.token}")
            .header("Content-Type", "application/json")
            .bodyValue(mapOf("query" to query))
            .retrieve()
            .bodyToMono<Map<String, Any>>()
            .block() ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

        val errors = response["errors"] as? List<*>
        if (errors != null && errors.isNotEmpty()) {
            val errorMessage = (errors[0] as? Map<*, *>)?.get("message") as? String ?: "Unknown GraphQL error"
            throw RuntimeException("GitHub GraphQL error: $errorMessage")
        }

        return response
    }

    private fun buildFullUserQuery(username: String, years: List<Int>): String {
        val contributionFragments = buildContributionFragments(years)

        return """
            {
                user(login: "$username") {
                    name
                    bio
                    avatarUrl
                    createdAt
                    repositories(ownerAffiliations: OWNER) {
                        totalCount
                    }
                    $contributionFragments
                }
            }
        """.trimIndent()
    }

    private fun buildContributionsOnlyQuery(username: String, years: List<Int>): String {
        val contributionFragments = buildContributionFragments(years)

        return """
            {
                user(login: "$username") {
                    $contributionFragments
                }
            }
        """.trimIndent()
    }

    private fun buildContributionFragments(years: List<Int>): String {
        return years.joinToString("\n") { year ->
            """
                    y$year: contributionsCollection(from: "${year}-01-01T00:00:00Z", to: "${year}-12-31T23:59:59Z") {
                        totalCommitContributions
                        contributionCalendar {
                            weeks {
                                contributionDays {
                                    contributionCount
                                    date
                                }
                            }
                        }
                    }
            """.trimIndent()
        }
    }

    private fun extractUser(response: Map<String, Any>): Map<*, *> {
        val data = response["data"] as? Map<*, *>
            ?: throw CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
        return data["user"] as? Map<*, *>
            ?: throw CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
    }

    private fun parseFullUserResponse(response: Map<String, Any>, years: List<Int>): GithubUserData {
        val user = extractUser(response)

        val name = user["name"] as? String
        val bio = user["bio"] as? String
        val avatarUrl = user["avatarUrl"] as? String ?: ""
        val createdAtStr = user["createdAt"] as? String ?: ""
        val createdAt = LocalDate.parse(createdAtStr.substring(0, 10))

        val repositories = user["repositories"] as? Map<*, *>
        val repositoryCount = repositories?.get("totalCount") as? Int ?: 0

        val (totalCommits, contributionDays) = extractContributions(user, years)

        return GithubUserData(
            name = name,
            bio = bio,
            avatarUrl = avatarUrl,
            createdAt = createdAt,
            repositoryCount = repositoryCount,
            totalCommits = totalCommits,
            contributionDays = contributionDays.toMutableList()
        )
    }

    private fun parseContributions(response: Map<String, Any>, years: List<Int>): Pair<Int, List<ContributionDay>> {
        val data = response["data"] as? Map<*, *> ?: return Pair(0, emptyList())
        val user = data["user"] as? Map<*, *> ?: return Pair(0, emptyList())
        return extractContributions(user, years)
    }

    private fun extractContributions(user: Map<*, *>, years: List<Int>): Pair<Int, List<ContributionDay>> {
        var totalCommits = 0
        val allDays = mutableListOf<ContributionDay>()

        for (year in years) {
            val yearData = user["y$year"] as? Map<*, *> ?: continue
            val commitCount = yearData["totalCommitContributions"] as? Int ?: 0
            totalCommits += commitCount

            val calendar = yearData["contributionCalendar"] as? Map<*, *> ?: continue
            val weeks = calendar["weeks"] as? List<*> ?: continue

            for (week in weeks) {
                val weekMap = week as? Map<*, *> ?: continue
                val days = weekMap["contributionDays"] as? List<*> ?: continue

                for (day in days) {
                    val dayMap = day as? Map<*, *> ?: continue
                    val count = dayMap["contributionCount"] as? Int ?: 0
                    val dateStr = dayMap["date"] as? String ?: continue

                    allDays.add(ContributionDay(
                        date = LocalDate.parse(dateStr),
                        count = count
                    ))
                }
            }
        }

        return Pair(totalCommits, allDays)
    }
}
