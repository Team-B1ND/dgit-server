package dodam.b1nd.dgit.global.exception

class CustomException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)
