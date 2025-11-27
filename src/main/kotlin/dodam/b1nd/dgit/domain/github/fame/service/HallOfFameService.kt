package dodam.b1nd.dgit.domain.github.fame.service

import dodam.b1nd.dgit.domain.github.fame.dto.response.HallOfFameResponse
import dodam.b1nd.dgit.domain.github.fame.repository.WeeklyRecordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class HallOfFameService(
    private val weeklyRecordRepository: WeeklyRecordRepository
) {

    fun getHallOfFame(): List<HallOfFameResponse> {
        val allRecords = weeklyRecordRepository.findAllOrderByWeekCommitsDesc()

        if (allRecords.isEmpty()) {
            return emptyList()
        }

        val maxCommits = allRecords.first().weekCommits

        val recordsWithFirstPlaceCount = allRecords.map { record ->
            val userRecords = weeklyRecordRepository.findByGithubAccount(record.githubAccount)
            val firstPlaceCount = userRecords.count { it.weekCommits == maxCommits }

            record to firstPlaceCount
        }

        return recordsWithFirstPlaceCount.mapIndexed { index, (record, firstPlaceCount) ->
            HallOfFameResponse(
                rank = index + 1,
                weekStart = record.weekStart,
                weekCommits = record.weekCommits,
                avatarUrl = record.githubAccount.avatarUrl,
                username = record.githubAccount.username,
                name = record.githubAccount.name,
                bio = record.githubAccount.bio,
                firstPlaceCount = firstPlaceCount
            )
        }
    }
}
