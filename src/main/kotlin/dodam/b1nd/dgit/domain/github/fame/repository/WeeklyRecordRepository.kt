package dodam.b1nd.dgit.domain.github.fame.repository

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.fame.entity.WeeklyRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface WeeklyRecordRepository : JpaRepository<WeeklyRecord, Long> {
    fun existsByGithubAccountAndWeekStart(githubAccount: GithubAccount, weekStart: LocalDate): Boolean

    @Query("SELECT w FROM WeeklyRecord w ORDER BY w.weekCommits DESC")
    fun findAllOrderByWeekCommitsDesc(): List<WeeklyRecord>

    @Query("SELECT w FROM WeeklyRecord w ORDER BY w.weekCommits DESC")
    fun findAllOrderByWeekCommitsDesc(pageable: Pageable): Page<WeeklyRecord>

    fun findByGithubAccount(githubAccount: GithubAccount): List<WeeklyRecord>

    @Query("SELECT DISTINCT w.weekStart FROM WeeklyRecord w ORDER BY w.weekStart")
    fun findAllDistinctWeekStarts(): List<LocalDate>

    fun findByWeekStart(weekStart: LocalDate): List<WeeklyRecord>
}
