package dodam.b1nd.dgit.domain.user.repository

import dodam.b1nd.dgit.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}
