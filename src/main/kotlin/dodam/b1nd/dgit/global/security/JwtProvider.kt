package dodam.b1nd.dgit.global.security

import dodam.b1nd.dgit.domain.user.enums.Role
import dodam.b1nd.dgit.global.config.JwtProperties
import dodam.b1nd.dgit.global.exception.CustomException
import dodam.b1nd.dgit.global.exception.ErrorCode
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties
) {

    private val secretKey = Keys.hmacShaKeyFor(
        jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8)
    )

    fun generateToken(
        email: String,
        role: Role,
        expireTime: Long,
        tokenType: String
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expireTime)

        return Jwts.builder()
            .claims(
                mapOf(
                    "email" to email,
                    "role" to role.name,
                    "type" to tokenType
                )
            )
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Claims {
        try {
            return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw CustomException(ErrorCode.TOKEN_EXPIRED)
        } catch (e: IllegalArgumentException) {
            throw CustomException(ErrorCode.TOKEN_NOT_PROVIDED)
        } catch (e: UnsupportedJwtException) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        } catch (e: MalformedJwtException) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
    }

    fun getEmailFromToken(token: String): String {
        val claims = validateToken(token)
        return claims["email"] as String
    }
}
