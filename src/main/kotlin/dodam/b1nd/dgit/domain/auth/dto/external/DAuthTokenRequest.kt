package dodam.b1nd.dgit.domain.auth.dto.external

data class DAuthTokenRequest(
    val code: String,
    val clientSecret: String
)
