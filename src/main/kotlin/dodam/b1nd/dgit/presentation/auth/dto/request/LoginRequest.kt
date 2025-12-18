package dodam.b1nd.dgit.presentation.auth.dto.request

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Authorization code는 필수입니다")
    val code: String
)
