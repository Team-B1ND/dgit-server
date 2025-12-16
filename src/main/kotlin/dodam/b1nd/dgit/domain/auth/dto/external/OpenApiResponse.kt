package dodam.b1nd.dgit.domain.auth.dto.external

import dodam.b1nd.dgit.domain.user.enums.Role

data class OpenApiResponse(
    val message: String,
    val data: UserInfo
)

data class UserInfo(
    val sub: String,
    val name: String,
    val role: Role,
    val profileImage: String? = null,
    val email: String,
    val phone: String? = null
)