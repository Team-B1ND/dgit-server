package dodam.b1nd.dgit.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token은 필수입니다")
    val refreshToken: String
)
