package dodam.b1nd.dgit.domain.github.account.entity

import dodam.b1nd.dgit.domain.user.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "github_accounts")
data class GithubAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "github_account_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = true)
    var name: String? = null,

    @Column(nullable = true, length = 1000)
    var bio: String? = null,

    @Column(nullable = true)
    var avatarUrl: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
