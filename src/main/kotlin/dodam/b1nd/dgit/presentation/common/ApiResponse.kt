package dodam.b1nd.dgit.presentation.common

import org.springframework.http.HttpStatus

data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(
            status: HttpStatus = HttpStatus.OK,
            message: String = "성공",
            data: T? = null
        ): ApiResponse<T> {
            return ApiResponse(
                status = status.value(),
                message = message,
                data = data
            )
        }

        fun <T> error(
            status: HttpStatus,
            message: String
        ): ApiResponse<T> {
            return ApiResponse(
                status = status.value(),
                message = message,
                data = null
            )
        }
    }
}
