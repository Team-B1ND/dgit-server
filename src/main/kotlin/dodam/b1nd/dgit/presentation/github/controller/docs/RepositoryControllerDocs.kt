package dodam.b1nd.dgit.presentation.github.controller.docs

import dodam.b1nd.dgit.presentation.github.dto.request.RegisterRepositoryRequest
import dodam.b1nd.dgit.presentation.github.dto.response.RepositoryResponse
import dodam.b1nd.dgit.presentation.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Repository", description = "레포지토리 관리 API")
interface RepositoryControllerDocs {

    @Operation(
        summary = "레포지토리 등록",
        description = "Github 레포지토리를 등록합니다. 관리자 승인 후 랭킹에 반영됩니다."
    )
    fun registerRepository(@Valid @RequestBody request: RegisterRepositoryRequest): ApiResponse<RepositoryResponse>

    @Operation(
        summary = "레포지토리 승인",
        description = "등록된 레포지토리를 승인합니다. (관리자 전용)"
    )
    fun approveRepository(@PathVariable repositoryId: Long): ApiResponse<RepositoryResponse>
}
