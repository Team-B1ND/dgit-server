package dodam.b1nd.dgit.domain.github.fame.repository

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.fame.entity.WeeklyRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface WeeklyRecordRepository : JpaRepository<WeeklyRecord, Long> {
    fun existsByGithubAccountAndWeekStart(githubAccount: GithubAccount, weekStart: LocalDate): Boolean

    @Query("SELECT w FROM WeeklyRecord w ORDER BY w.weekCommits DESC")
    fun findAllOrderByWeekCommitsDesc(): List<WeeklyRecord>

    fun findByGithubAccount(githubAccount: GithubAccount): List<WeeklyRecord>
}
