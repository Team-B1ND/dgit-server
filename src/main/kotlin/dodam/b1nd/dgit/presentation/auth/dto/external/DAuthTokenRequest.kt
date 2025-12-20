package dodam.b1nd.dgit.presentation.auth.dto.external

@Deprecated("OAuth 2.0 표준 form-urlencoded 요청으로 대체됨")
data class DAuthTokenRequest(
    val code: String,
    val clientSecret: String
)
