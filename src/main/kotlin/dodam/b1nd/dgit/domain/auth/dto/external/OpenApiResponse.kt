package dodam.b1nd.dgit.domain.auth.dto.external

data class OpenApiResponse(
    val message: String,
    val data: UserInfo
)

data class UserInfo(
    val grade: Int,
    val room: Int,
    val number: Int,
    val name: String,
    val profileImage: String,
    val email: String
)
