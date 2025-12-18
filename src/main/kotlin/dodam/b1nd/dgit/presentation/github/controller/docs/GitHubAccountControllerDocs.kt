package dodam.b1nd.dgit.presentation.github.controller.docs

import dodam.b1nd.dgit.presentation.github.dto.request.RegisterGithubAccountRequest
import dodam.b1nd.dgit.presentation.github.dto.response.GithubAccountResponse
import dodam.b1nd.dgit.presentation.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Github Account", description = "Github 계정 관리 API")
interface GitHubAccountControllerDocs {

    @Operation(
        summary = "Github 계정 등록",
        description = "Github 아이디를 등록합니다. 등록 후 자동으로 커밋 데이터가 수집됩니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    fun registerGithubAccount(@Valid @RequestBody request: RegisterGithubAccountRequest): ApiResponse<GithubAccountResponse>

    @Operation(
        summary = "내 Github 계정 목록 조회",
        description = "로그인한 사용자의 등록된 Github 계정 목록을 조회합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    fun getMyGithubAccounts(): ApiResponse<List<GithubAccountResponse>>
}
