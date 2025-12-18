package dodam.b1nd.dgit.application.github

import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.presentation.github.dto.request.RegisterRepositoryRequest
import dodam.b1nd.dgit.presentation.github.dto.response.RepositoryResponse
import dodam.b1nd.dgit.domain.github.repository.entity.Repository
import dodam.b1nd.dgit.domain.github.repository.repository.RepositoryRepository
import dodam.b1nd.dgit.infrastructure.client.GithubClient
import dodam.b1nd.dgit.infrastructure.exception.CustomException
import dodam.b1nd.dgit.infrastructure.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class RepositoryService(
    private val repositoryRepository: RepositoryRepository,
    private val githubAccountRepository: GithubAccountRepository,
    private val githubClient: GithubClient
) {

    @Transactional
    fun registerRepository(request: RegisterRepositoryRequest): RepositoryResponse {
        val githubAccount = githubAccountRepository.findById(request.githubAccountId).orElseThrow {
            CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)
        }

        if (repositoryRepository.existsByOwnerAndRepoName(request.owner, request.repoName)) {
            throw CustomException(ErrorCode.REPOSITORY_ALREADY_EXISTS)
        }

        val userInfo = githubClient.getUser(request.owner)

        val repository = Repository(
            githubAccount = githubAccount,
            owner = request.owner,
            repoName = request.repoName,
            ownerAvatarUrl = userInfo.avatarUrl
        )

        val saved = repositoryRepository.save(repository)
        return RepositoryResponse.from(saved)
    }

    @Transactional
    fun approveRepository(repositoryId: Long): RepositoryResponse {
        val repository = repositoryRepository.findById(repositoryId).orElseThrow {
            CustomException(ErrorCode.REPOSITORY_NOT_FOUND)
        }

        repository.isApproved = true
        updateRepositoryStats(repository)

        return RepositoryResponse.from(repository)
    }

    private fun updateRepositoryStats(repository: Repository) {
        try {
            val commits = githubClient.getRepositoryCommits(
                owner = repository.owner,
                repo = repository.repoName,
                author = repository.owner
            )
            repository.totalCommits = commits.size

            val repositories = githubClient.getUserRepositories(repository.owner)
            val repo = repositories.find { it.name == repository.repoName }
            if (repo != null) {
                repository.stars = 0
            }
        } catch (_: Exception) {
        }
    }
}
