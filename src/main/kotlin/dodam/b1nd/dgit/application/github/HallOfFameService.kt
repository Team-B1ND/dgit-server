package dodam.b1nd.dgit.application.github

import dodam.b1nd.dgit.presentation.github.dto.response.FirstPlaceRankingResponse
import dodam.b1nd.dgit.presentation.github.dto.response.HallOfFameResponse
import dodam.b1nd.dgit.domain.github.fame.repository.WeeklyRecordRepository
import dodam.b1nd.dgit.presentation.common.PageResponse
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class HallOfFameService(
    private val weeklyRecordRepository: WeeklyRecordRepository
) {

    fun getHallOfFame(pageable: Pageable): PageResponse<HallOfFameResponse> {
        val recordsPage = weeklyRecordRepository.findAllOrderByWeekCommitsDesc(pageable)

        if (recordsPage.isEmpty) {
            return PageResponse(
                content = emptyList(),
                totalElements = 0,
                totalPages = 0,
                currentPage = 0,
                size = pageable.pageSize,
                hasNext = false,
                hasPrevious = false
            )
        }

        val allWeekStarts = weeklyRecordRepository.findAllDistinctWeekStarts()
        val weekMaxCommits = allWeekStarts.associateWith { weekStart ->
            weeklyRecordRepository.findByWeekStart(weekStart)
                .maxOfOrNull { it.weekCommits } ?: 0
        }

        val responses = recordsPage.content.mapIndexed { index, record ->
            val userRecords = weeklyRecordRepository.findByGithubAccount(record.githubAccount)

            val firstPlaceCount = userRecords.count { userRecord ->
                val maxCommitsForWeek = weekMaxCommits[userRecord.weekStart] ?: 0
                userRecord.weekCommits == maxCommitsForWeek && maxCommitsForWeek > 0
            }

            HallOfFameResponse(
                rank = recordsPage.number * recordsPage.size + index + 1,
                weekStart = record.weekStart,
                weekCommits = record.weekCommits,
                avatarUrl = record.githubAccount.avatarUrl,
                githubName = record.githubAccount.username,
                name = record.githubAccount.name,
                bio = record.githubAccount.bio,
                firstPlaceCount = firstPlaceCount
            )
        }

        return PageResponse(
            content = responses,
            totalElements = recordsPage.totalElements,
            totalPages = recordsPage.totalPages,
            currentPage = recordsPage.number,
            size = recordsPage.size,
            hasNext = recordsPage.hasNext(),
            hasPrevious = recordsPage.hasPrevious()
        )
    }

    fun getFirstPlaceRankings(): List<FirstPlaceRankingResponse> {
        val allWeekStarts = weeklyRecordRepository.findAllDistinctWeekStarts()
        val weekMaxCommits = allWeekStarts.associateWith { weekStart ->
            weeklyRecordRepository.findByWeekStart(weekStart)
                .maxOfOrNull { it.weekCommits } ?: 0
        }

        val allRecords = weeklyRecordRepository.findAll()

        val userFirstPlaceCounts = allRecords
            .groupBy { it.githubAccount }
            .mapValues { (_, records) ->
                records.count { record ->
                    val maxCommitsForWeek = weekMaxCommits[record.weekStart] ?: 0
                    record.weekCommits == maxCommitsForWeek && maxCommitsForWeek > 0
                }
            }
            .filter { it.value > 0 }

        return userFirstPlaceCounts
            .map { (githubAccount, count) ->
                FirstPlaceRankingResponse(
                    githubName = githubAccount.username,
                    name = githubAccount.name,
                    avatarUrl = githubAccount.avatarUrl,
                    firstPlaceCount = count
                )
            }
            .sortedByDescending { it.firstPlaceCount }
    }
}
