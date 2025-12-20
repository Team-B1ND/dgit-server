package dodam.b1nd.dgit.presentation.auth.dto.external

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

data class BasicResponse<T>(
    val status: Int,
    val message: String,
    val data: T?
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DAuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val idToken: String? = null,
    val tokenType: String,
    val expiresIn: Long? = null,
    val scope: String? = null
)
