package dodam.b1nd.dgit.presentation.github.controller

import dodam.b1nd.dgit.application.github.usecase.RepositoryUseCase
import dodam.b1nd.dgit.presentation.common.ApiResponse
import dodam.b1nd.dgit.presentation.github.controller.docs.RepositoryControllerDocs
import dodam.b1nd.dgit.presentation.github.dto.request.RegisterRepositoryRequest
import dodam.b1nd.dgit.presentation.github.dto.response.RepositoryResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/repository")
class RepositoryController(
    private val repositoryUseCase: RepositoryUseCase
) : RepositoryControllerDocs {

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @PostMapping("/register")
    override fun registerRepository(
        @Valid @RequestBody request: RegisterRepositoryRequest
    ): ApiResponse<RepositoryResponse> {
        val response = repositoryUseCase.registerRepository(request)
        return ApiResponse.success(
            status = HttpStatus.CREATED,
            message = "레포지토리 등록 성공",
            data = response
        )
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{repositoryId}/approve")
    override fun approveRepository(
        @PathVariable repositoryId: Long
    ): ApiResponse<RepositoryResponse> {
        val response = repositoryUseCase.approveRepository(repositoryId)
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "레포지토리 승인 성공",
            data = response
        )
    }
}
