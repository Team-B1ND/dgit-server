package dodam.b1nd.dgit.presentation.auth.controller.docs

import dodam.b1nd.dgit.presentation.auth.dto.request.LoginRequest
import dodam.b1nd.dgit.presentation.auth.dto.request.RefreshTokenRequest
import dodam.b1nd.dgit.presentation.auth.dto.response.RefreshTokenResponse
import dodam.b1nd.dgit.presentation.auth.dto.response.TokenResponse
import dodam.b1nd.dgit.presentation.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Auth", description = "OAuth 2.0 인증 API")
interface AuthControllerDocs {

    @Operation(
        summary = "OAuth 2.0 로그인",
        description = """
            DAuth OAuth 2.0 Authorization Code를 사용하여 로그인합니다.

            **인증 흐름:**
            1. 클라이언트가 DAuth Authorization 엔드포인트로 리다이렉트
               - URL: https://dauthapi.b1nd.com/oauth/authorize
               - 파라미터: client_id, redirect_uri, response_type=code, scope
            2. 사용자 로그인 후 Authorization Code 획득
            3. 이 엔드포인트에 Code 전송
            4. 서버가 DAuth와 OAuth 2.0 표준 토큰 교환 수행
            5. JWT Access Token 및 Refresh Token 반환

            **응답:**
            - accessToken: 3일 유효한 JWT 토큰
            - refreshToken: 180일 유효한 JWT 토큰
        """
    )
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<TokenResponse>

    @Operation(
        summary = "토큰 갱신",
        description = """
            Refresh Token으로 새로운 Access Token을 발급받습니다.

            **요청:**
            - refreshToken: 로그인 시 받은 Refresh Token

            **응답:**
            - accessToken: 새로 발급된 Access Token
        """
    )
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ApiResponse<RefreshTokenResponse>
}
