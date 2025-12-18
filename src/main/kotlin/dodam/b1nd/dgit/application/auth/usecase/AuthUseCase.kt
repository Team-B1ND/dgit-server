package dodam.b1nd.dgit.application.auth.usecase

import dodam.b1nd.dgit.application.auth.AuthService
import dodam.b1nd.dgit.application.token.TokenService
import dodam.b1nd.dgit.application.user.UserService
import dodam.b1nd.dgit.domain.user.entity.User
import dodam.b1nd.dgit.domain.user.enums.Role
import dodam.b1nd.dgit.presentation.auth.dto.request.LoginRequest
import dodam.b1nd.dgit.presentation.auth.dto.response.RefreshTokenResponse
import dodam.b1nd.dgit.presentation.auth.dto.response.TokenResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AuthUseCase(
    private val authService: AuthService,
    private val userService: UserService,
    private val tokenService: TokenService
) {

    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        val dAuthTokenResponse = authService.exchangeCodeForToken(request.code)
        val userInfo = authService.getUserInfo(dAuthTokenResponse.accessToken)

        val user = userService.saveOrUpdate(
            User(
                email = userInfo.email,
                name = userInfo.name,
                role = userInfo.role,
                dodamId = userInfo.sub,
                dodamRefreshToken = dAuthTokenResponse.refreshToken
            )
        )

        return TokenResponse(
            accessToken = tokenService.generateAccessToken(user.email, user.role),
            refreshToken = tokenService.generateRefreshToken(user.email, user.role)
        )
    }

    fun refreshToken(email: String, role: Role): RefreshTokenResponse {
        return RefreshTokenResponse(
            accessToken = tokenService.generateAccessToken(email, role)
        )
    }
}
