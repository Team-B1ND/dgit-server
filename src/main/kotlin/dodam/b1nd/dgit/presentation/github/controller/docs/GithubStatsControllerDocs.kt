package dodam.b1nd.dgit.presentation.github.controller.docs

import dodam.b1nd.dgit.presentation.github.dto.response.GithubStatsResponse
import dodam.b1nd.dgit.presentation.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Github Stats", description = "Github 통계 API")
interface GithubStatsControllerDocs {

    @Operation(
        summary = "Github 계정 통계 조회",
        description = "특정 Github 계정의 통계를 조회합니다. 전체 커밋 수, 오늘/이번주 커밋 수, 스트릭, 랭킹 등을 반환합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    fun getGithubAccountStats(@PathVariable githubAccountId: Long): ApiResponse<GithubStatsResponse>
}
