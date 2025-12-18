package dodam.b1nd.dgit.infrastructure.exception

class CustomException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)
