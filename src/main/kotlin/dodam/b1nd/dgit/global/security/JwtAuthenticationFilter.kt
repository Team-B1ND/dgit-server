package dodam.b1nd.dgit.global.security

import dodam.b1nd.dgit.domain.user.entity.User
import dodam.b1nd.dgit.domain.user.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val userService: UserService,
) : OncePerRequestFilter() {
    companion object {
        private const val TOKEN_TYPE = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)

            if (authHeader != null && authHeader.startsWith(TOKEN_TYPE)) {
                val token = authHeader.removePrefix(TOKEN_TYPE)
                val claims = jwtProvider.validateToken(token)
                val email = claims["email"] as String
                val user = userService.getUserByEmail(email)

                val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                val authentication = UsernamePasswordAuthenticationToken(user, null, authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (_: Exception) {
            // 토큰 검증 실패 시 인증 없이 진행 (Spring Security가 처리)
        }

        filterChain.doFilter(request, response)
    }
}
