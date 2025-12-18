package dodam.b1nd.dgit.presentation.github.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "레포지토리 등록 요청")
data class RegisterRepositoryRequest(
    @field:NotNull(message = "Github 계정 ID를 입력해주세요")
    @Schema(description = "등록할 Github 계정 ID", example = "1")
    val githubAccountId: Long,

    @field:NotBlank(message = "소유자 아이디를 입력해주세요")
    @Schema(description = "Github 사용자 아이디 또는 조직 이름", example = "octocat")
    val owner: String,

    @field:NotBlank(message = "레포지토리 이름을 입력해주세요")
    @Schema(description = "레포지토리 이름", example = "Hello-World")
    val repoName: String
)
