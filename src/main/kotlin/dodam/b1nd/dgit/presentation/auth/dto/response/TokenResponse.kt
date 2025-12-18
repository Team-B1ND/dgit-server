package dodam.b1nd.dgit.presentation.auth.dto.response

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)
