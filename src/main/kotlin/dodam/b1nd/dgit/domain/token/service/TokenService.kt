package dodam.b1nd.dgit.domain.token.service

import dodam.b1nd.dgit.domain.user.enums.Role
import dodam.b1nd.dgit.global.security.JwtProvider
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val jwtProvider: JwtProvider
) {
    companion object {
        private const val ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 3  // 3일
        private const val REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 180  // 180일
    }

    fun generateAccessToken(email: String, role: Role): String {
        return jwtProvider.generateToken(
            email = email,
            role = role,
            expireTime = ACCESS_TOKEN_EXPIRE_TIME,
            tokenType = "ACCESS"
        )
    }

    fun generateRefreshToken(email: String, role: Role): String {
        return jwtProvider.generateToken(
            email = email,
            role = role,
            expireTime = REFRESH_TOKEN_EXPIRE_TIME,
            tokenType = "REFRESH"
        )
    }
}
