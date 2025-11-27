package dodam.b1nd.dgit.domain.github.repository.repository

import dodam.b1nd.dgit.domain.github.repository.entity.Repository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RepositoryRepository : JpaRepository<Repository, Long> {
    fun existsByOwnerAndRepoName(owner: String, repoName: String): Boolean
    fun findByOwnerAndRepoName(owner: String, repoName: String): Repository?

    @Query("SELECT r FROM Repository r WHERE r.isApproved = true ORDER BY r.totalCommits DESC")
    fun findAllApprovedOrderByTotalCommitsDesc(): List<Repository>
}
