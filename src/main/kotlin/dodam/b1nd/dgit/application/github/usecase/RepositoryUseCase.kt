package dodam.b1nd.dgit.application.github.usecase

import dodam.b1nd.dgit.application.github.RepositoryService
import dodam.b1nd.dgit.presentation.github.dto.request.RegisterRepositoryRequest
import dodam.b1nd.dgit.presentation.github.dto.response.RepositoryResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RepositoryUseCase(
    private val repositoryService: RepositoryService
) {

    @Transactional
    fun registerRepository(request: RegisterRepositoryRequest): RepositoryResponse {
        val saved = repositoryService.register(
            githubAccountId = request.githubAccountId,
            owner = request.owner,
            repoName = request.repoName
        )
        return RepositoryResponse.from(saved)
    }

    @Transactional
    fun approveRepository(repositoryId: Long): RepositoryResponse {
        val repository = repositoryService.approve(repositoryId)
        return RepositoryResponse.from(repository)
    }
}
