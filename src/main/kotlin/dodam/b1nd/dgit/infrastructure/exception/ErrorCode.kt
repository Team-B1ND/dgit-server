package dodam.b1nd.dgit.infrastructure.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String
) {
    // 인증
    TOKEN_NOT_PROVIDED(HttpStatus.UNAUTHORIZED, "토큰이 제공되지 않았습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),

    // Github 계정
    GITHUB_ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 Github 계정입니다"),
    GITHUB_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 Github 계정을 찾을 수 없습니다"),
    GITHUB_USERNAME_ALREADY_TAKEN(HttpStatus.CONFLICT, "이미 사용 중인 Github 아이디입니다"),

    REPOSITORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 레포지토리입니다"),
    REPOSITORY_NOT_FOUND(HttpStatus.NOT_FOUND, "레포지토리를 찾을 수 없습니다"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다")
}
