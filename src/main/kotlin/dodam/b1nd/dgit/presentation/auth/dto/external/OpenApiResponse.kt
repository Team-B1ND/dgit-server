package dodam.b1nd.dgit.presentation.auth.dto.external

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dodam.b1nd.dgit.domain.user.enums.Role

data class OpenApiResponse(
    val message: String,
    val data: UserInfo
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserInfo(
    val sub: String,
    val name: String,
    val role: Role,
    val profileImage: String? = null,
    val email: String,
    val phone: String? = null
)