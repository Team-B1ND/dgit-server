package dodam.b1nd.dgit.global.security

import dodam.b1nd.dgit.domain.user.entity.User
import org.springframework.security.core.context.SecurityContextHolder

object UserAuthenticationHolder {
    fun current(): User {
        return SecurityContextHolder.getContext().authentication.principal as User
    }
}