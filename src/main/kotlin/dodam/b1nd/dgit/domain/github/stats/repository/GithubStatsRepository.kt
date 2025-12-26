package dodam.b1nd.dgit.domain.github.stats.repository

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.github.stats.entity.GithubStats
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GithubStatsRepository : JpaRepository<GithubStats, Long> {
    fun findByGithubAccount(githubAccount: GithubAccount): GithubStats?

    /**
     * 총 커밋 수 기준으로 랭킹 조회 (내림차순)
     */
    @Query("SELECT s FROM GithubStats s ORDER BY s.totalCommits DESC")
    fun findAllOrderByTotalCommitsDesc(): List<GithubStats>

    /**
     * 특정 사용자보다 커밋 수가 많은 사용자 수 조회 (랭킹 계산용)
     */
    @Query("SELECT COUNT(s) FROM GithubStats s WHERE s.totalCommits > :totalCommits")
    fun countByTotalCommitsGreaterThan(totalCommits: Int): Long
}
