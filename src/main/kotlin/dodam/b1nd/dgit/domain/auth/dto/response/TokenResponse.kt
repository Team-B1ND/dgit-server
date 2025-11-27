package dodam.b1nd.dgit.domain.auth.dto.response

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)
