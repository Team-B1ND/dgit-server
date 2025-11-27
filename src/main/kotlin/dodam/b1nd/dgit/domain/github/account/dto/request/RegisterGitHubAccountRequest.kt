package dodam.b1nd.dgit.domain.github.account.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@Schema(description = "Github 계정 등록 요청")
data class RegisterGithubAccountRequest(
    @field:NotBlank(message = "Github 아이디를 입력해주세요")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}\$",
        message = "올바른 Github 아이디 형식이 아닙니다"
    )
    @Schema(description = "Github 사용자 아이디", example = "octocat")
    val username: String
)
