package dodam.b1nd.dgit.domain.auth.controller.docs

import dodam.b1nd.dgit.domain.auth.dto.request.LoginRequest
import dodam.b1nd.dgit.domain.auth.dto.request.RefreshTokenRequest
import dodam.b1nd.dgit.domain.auth.dto.response.RefreshTokenResponse
import dodam.b1nd.dgit.domain.auth.dto.response.TokenResponse
import dodam.b1nd.dgit.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Auth", description = "인증 API")
interface AuthControllerDocs {

    @Operation(
        summary = "로그인",
        description = "DAuth Authorization Code로 로그인합니다. 성공 시 Access Token과 Refresh Token을 반환합니다."
    )
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<TokenResponse>

    @Operation(
        summary = "토큰 갱신",
        description = "Refresh Token으로 새로운 Access Token을 발급받습니다."
    )
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ApiResponse<RefreshTokenResponse>
}
