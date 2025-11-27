package dodam.b1nd.dgit.domain.github.repository.controller

import dodam.b1nd.dgit.domain.github.repository.dto.request.RegisterRepositoryRequest
import dodam.b1nd.dgit.domain.github.repository.dto.response.RepositoryResponse
import dodam.b1nd.dgit.domain.github.repository.service.RepositoryService
import dodam.b1nd.dgit.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Repository", description = "레포지토리 관리 API")
@RestController
@RequestMapping("/repository")
class RepositoryController(
    private val repositoryService: RepositoryService
) {

    @Operation(
        summary = "레포지토리 등록",
        description = "Github 레포지토리를 등록합니다. 관리자 승인 후 랭킹에 반영됩니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @PostMapping("/register")
    fun registerRepository(
        @Valid @RequestBody request: RegisterRepositoryRequest
    ): ApiResponse<RepositoryResponse> {
        val response = repositoryService.registerRepository(request)
        return ApiResponse.success(
            status = HttpStatus.CREATED,
            message = "레포지토리 등록 성공",
            data = response
        )
    }

    @Operation(
        summary = "레포지토리 승인",
        description = "등록된 레포지토리를 승인합니다. (관리자 전용)",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{repositoryId}/approve")
    fun approveRepository(
        @PathVariable repositoryId: Long
    ): ApiResponse<RepositoryResponse> {
        val response = repositoryService.approveRepository(repositoryId)
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "레포지토리 승인 성공",
            data = response
        )
    }
}
