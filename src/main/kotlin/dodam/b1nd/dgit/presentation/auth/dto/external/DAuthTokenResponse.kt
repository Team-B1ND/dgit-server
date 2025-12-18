package dodam.b1nd.dgit.presentation.auth.dto.external

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

data class BasicResponse<T>(
    val status: Int,
    val message: String,
    val data: T?
)

data class DAuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val idToken: String,
    val tokenType: String
)
