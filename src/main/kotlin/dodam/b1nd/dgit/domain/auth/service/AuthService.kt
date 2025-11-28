package dodam.b1nd.dgit.domain.auth.service

import dodam.b1nd.dgit.domain.auth.dto.request.LoginRequest
import dodam.b1nd.dgit.domain.auth.dto.response.RefreshTokenResponse
import dodam.b1nd.dgit.domain.auth.dto.response.TokenResponse
import dodam.b1nd.dgit.domain.token.service.TokenService
import dodam.b1nd.dgit.domain.user.entity.User
import dodam.b1nd.dgit.domain.user.enums.Role
import dodam.b1nd.dgit.domain.user.service.UserService
import dodam.b1nd.dgit.global.client.DAuthClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthService(
    private val dAuthClient: DAuthClient,
    private val userService: UserService,
    private val tokenService: TokenService
) {

    /**
     * 로그인 플로우
     * 1. Authorization Code → DAuth AccessToken
     * 2. DAuth AccessToken → 사용자 정보 조회
     * 3. 사용자 DB 저장/업데이트
     * 4. DGIT JWT 토큰 생성
     */
    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        val dAuthAccessToken = dAuthClient.exchangeCodeForToken(request.code)
        val userInfo = dAuthClient.getUserInfo(dAuthAccessToken)
        val user = userService.saveOrUpdate(
            User(
                email = userInfo.email,
                name = userInfo.name,
                role = Role.STUDENT
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
