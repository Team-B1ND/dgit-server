package dodam.b1nd.dgit.domain.github.repository.entity

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "github_repositories")
data class Repository(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repository_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_account_id", nullable = false)
    val githubAccount: GithubAccount,

    @Column(nullable = false)
    val owner: String,

    @Column(nullable = false)
    val repoName: String,

    @Column(nullable = false)
    var isApproved: Boolean = false,

    @Column(nullable = false)
    var totalCommits: Int = 0,

    @Column(nullable = false)
    var stars: Int = 0,

    @Column(nullable = true)
    var ownerAvatarUrl: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
