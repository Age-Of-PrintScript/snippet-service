package age.of.printscript.demo.shared.infrastructure.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filtro que corre inmediatamente DESPUÉS de que Spring Security valida el Bearer JWT.
 *
 * Extrae el subject del JWT (user ID) y lo carga en MDC["user-id"].
 */
class UserMdcFilter : OncePerRequestFilter() {
    companion object {
        const val USER_ID_KEY = "user-id"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val auth = SecurityContextHolder.getContext().authentication
        val jwt = auth?.principal as? Jwt
        if (jwt != null && !jwt.subject.isNullOrBlank()) {
            MDC.put(USER_ID_KEY, jwt.subject)
        }

        filterChain.doFilter(request, response)
    }
}
