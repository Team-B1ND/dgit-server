package dodam.b1nd.dgit.domain.auth.dto.external

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DAuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: String
)
