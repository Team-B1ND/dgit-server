package dodam.b1nd.dgit.domain.github.stats.entity

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "github_stats")
data class GithubStats(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stats_id")
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_account_id", nullable = false, unique = true)
    val githubAccount: GithubAccount,

    @Column(nullable = false)
    var todayCommits: Int = 0,

    @Column(nullable = false)
    var weekCommits: Int = 0,

    @Column(nullable = false)
    var totalCommits: Int = 0,

    @Column(nullable = false)
    var repositoryCount: Int = 0,

    @Column(nullable = false)
    var longestStreak: Int = 0,

    @Column(nullable = false)
    var currentStreak: Int = 0,

    @Column(nullable = false)
    var level: Int = 1,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
