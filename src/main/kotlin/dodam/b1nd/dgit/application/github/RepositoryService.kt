package dodam.b1nd.dgit.application.github

import dodam.b1nd.dgit.domain.github.account.repository.GithubAccountRepository
import dodam.b1nd.dgit.domain.github.repository.entity.Repository
import dodam.b1nd.dgit.domain.github.repository.repository.RepositoryRepository
import dodam.b1nd.dgit.infrastructure.client.GithubClient
import dodam.b1nd.dgit.infrastructure.exception.CustomException
import dodam.b1nd.dgit.infrastructure.exception.ErrorCode
import dodam.b1nd.dgit.infrastructure.security.UserAuthenticationHolder
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
    fun register(githubAccountId: Long, owner: String, repoName: String): Repository {
        val currentUser = UserAuthenticationHolder.current()
        val githubAccount = githubAccountRepository.findByIdAndUser(githubAccountId, currentUser)
            ?: throw CustomException(ErrorCode.GITHUB_ACCOUNT_NOT_FOUND)

        if (repositoryRepository.existsByOwnerAndRepoName(owner, repoName)) {
            throw CustomException(ErrorCode.REPOSITORY_ALREADY_EXISTS)
        }

        val userInfo = githubClient.getUser(owner)

        val repository = Repository(
            githubAccount = githubAccount,
            owner = owner,
            repoName = repoName,
            ownerAvatarUrl = userInfo.avatarUrl
        )

        return repositoryRepository.save(repository)
    }

    @Transactional
    fun approve(repositoryId: Long): Repository {
        val repository = repositoryRepository.findById(repositoryId).orElseThrow {
            CustomException(ErrorCode.REPOSITORY_NOT_FOUND)
        }

        repository.isApproved = true
        updateStats(repository)

        return repository
    }

    private fun updateStats(repository: Repository) {
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
