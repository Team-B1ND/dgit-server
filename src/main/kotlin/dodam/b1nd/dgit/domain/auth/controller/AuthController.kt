package dodam.b1nd.dgit.domain.auth.controller

import dodam.b1nd.dgit.domain.auth.dto.request.LoginRequest
import dodam.b1nd.dgit.domain.auth.dto.request.RefreshTokenRequest
import dodam.b1nd.dgit.domain.auth.dto.response.RefreshTokenResponse
import dodam.b1nd.dgit.domain.auth.dto.response.TokenResponse
import dodam.b1nd.dgit.domain.auth.service.AuthService
import dodam.b1nd.dgit.domain.user.enums.Role
import dodam.b1nd.dgit.global.response.ApiResponse
import dodam.b1nd.dgit.global.security.JwtProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtProvider: JwtProvider
) {

    @Operation(summary = "로그인", description = "DAuth Authorization Code로 로그인")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<TokenResponse> {
        val tokenResponse = authService.login(request)
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "로그인 성공",
            data = tokenResponse
        )
    }

    @Operation(summary = "토큰 갱신", description = "RefreshToken으로 AccessToken 재발급")
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ApiResponse<RefreshTokenResponse> {
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
