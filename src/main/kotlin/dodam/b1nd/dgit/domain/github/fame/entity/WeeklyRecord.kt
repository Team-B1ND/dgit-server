package dodam.b1nd.dgit.domain.github.fame.entity

import dodam.b1nd.dgit.domain.github.account.entity.GithubAccount
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "github_weekly_records",
    uniqueConstraints = [UniqueConstraint(columnNames = ["github_account_id", "week_start"])]
)
data class WeeklyRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_record_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_account_id", nullable = false)
    val githubAccount: GithubAccount,

    @Column(nullable = false)
    val weekStart: LocalDate,

    @Column(nullable = false)
    val weekCommits: Int,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
