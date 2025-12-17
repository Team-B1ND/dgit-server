package dodam.b1nd.dgit.domain.github.repository.controller

import dodam.b1nd.dgit.domain.github.repository.controller.docs.RepositoryControllerDocs
import dodam.b1nd.dgit.domain.github.repository.dto.request.RegisterRepositoryRequest
import dodam.b1nd.dgit.domain.github.repository.dto.response.RepositoryResponse
import dodam.b1nd.dgit.domain.github.repository.service.RepositoryService
import dodam.b1nd.dgit.global.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/repository")
class RepositoryController(
    private val repositoryService: RepositoryService
) : RepositoryControllerDocs {

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @PostMapping("/register")
    override fun registerRepository(
        @Valid @RequestBody request: RegisterRepositoryRequest
    ): ApiResponse<RepositoryResponse> {
        val response = repositoryService.registerRepository(request)
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
        val response = repositoryService.approveRepository(repositoryId)
        return ApiResponse.success(
            status = HttpStatus.OK,
            message = "레포지토리 승인 성공",
            data = response
        )
    }
}
