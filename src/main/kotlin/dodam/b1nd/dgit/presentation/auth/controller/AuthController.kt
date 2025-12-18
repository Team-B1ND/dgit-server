package dodam.b1nd.dgit.presentation.auth.controller

import dodam.b1nd.dgit.presentation.auth.controller.docs.AuthControllerDocs
import dodam.b1nd.dgit.presentation.auth.dto.request.LoginRequest
import dodam.b1nd.dgit.presentation.auth.dto.request.RefreshTokenRequest
import dodam.b1nd.dgit.presentation.auth.dto.response.RefreshTokenResponse
import dodam.b1nd.dgit.presentation.auth.dto.response.TokenResponse
import dodam.b1nd.dgit.application.auth.AuthService
import dodam.b1nd.dgit.domain.user.enums.Role
import dodam.b1nd.dgit.presentation.common.ApiResponse
import dodam.b1nd.dgit.infrastructure.security.JwtProvider
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtProvider: JwtProvider
) : AuthControllerDocs {

    @PostMapping("/login")
    override fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<TokenResponse> {
        val tokenResponse = authService.login(request)
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "로그인 성공",
            data = tokenResponse
        )
    }

    @PostMapping("/refresh")
    override fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ApiResponse<RefreshTokenResponse> {
        val claims = jwtProvider.validateToken(request.refreshToken)
        val email = claims["email"] as String
        val role = Role.valueOf(claims["role"] as String)

        val tokenType = claims["type"] as String
        require(tokenType == "REFRESH") { "RefreshToken이 아닙니다" }

        val response = authService.refreshToken(email, role)
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "토큰 갱신 성공",
            data = response
        )
    }
}
