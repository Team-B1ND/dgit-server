package dodam.b1nd.dgit.global.exception

import dodam.b1nd.dgit.global.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ApiResponse<Unit>> {
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ApiResponse.error(
                status = e.errorCode.status,
                message = e.errorCode.message
            ))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val errorMessage = e.bindingResult.allErrors.firstOrNull()?.defaultMessage ?: "유효하지 않은 입력입니다"
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(
                status = org.springframework.http.HttpStatus.BAD_REQUEST,
                message = errorMessage
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.error("Unexpected error occurred", e)
        return ResponseEntity
            .internalServerError()
            .body(ApiResponse.error(
                status = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                message = "서버 내부 오류가 발생했습니다"
            ))
    }
}
