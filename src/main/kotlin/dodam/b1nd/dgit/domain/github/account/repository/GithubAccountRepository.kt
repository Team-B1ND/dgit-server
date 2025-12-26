package dodam.b1nd.dgit.domain.github.account.repository

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import dodam.b1nd.dgit.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface GithubAccountRepository : JpaRepository<GithubAccount, Long> {
    fun existsByUsername(username: String): Boolean
    fun findAllByUser(user: User): List<GithubAccount>
    fun findByUsername(username: String): GithubAccount?
    fun findByIdAndUser(id: Long, user: User): GithubAccount?
}
