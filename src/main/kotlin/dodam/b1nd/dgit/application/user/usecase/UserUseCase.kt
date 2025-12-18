package dodam.b1nd.dgit.application.user.usecase

import dodam.b1nd.dgit.application.user.UserService
import dodam.b1nd.dgit.domain.user.entity.User
import org.springframework.stereotype.Component

@Component
class UserUseCase(
    private val userService: UserService
) {

    fun getUserByEmail(email: String): User {
        return userService.getUserByEmail(email)
    }
}
