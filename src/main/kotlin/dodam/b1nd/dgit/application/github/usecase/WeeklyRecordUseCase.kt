package dodam.b1nd.dgit.application.github.usecase

import dodam.b1nd.dgit.application.github.WeeklyRecordService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class WeeklyRecordUseCase(
    private val weeklyRecordService: WeeklyRecordService
) {

    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    fun saveLastWeekRecords() {
        weeklyRecordService.saveLastWeekRecords()
    }
}
