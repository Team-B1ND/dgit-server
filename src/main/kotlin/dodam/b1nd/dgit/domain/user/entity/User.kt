package dodam.b1nd.dgit.domain.user.entity

import dodam.b1nd.dgit.domain.user.enums.Role
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.STUDENT,

    @Column(nullable = false, unique = true)
    val dodamId: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var dodamRefreshToken: String
)
