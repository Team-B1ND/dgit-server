package dodam.b1nd.dgit.presentation.auth.dto.external

data class DAuthTokenRequest(
    val code: String,
    val clientSecret: String
)
