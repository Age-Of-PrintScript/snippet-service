package age.of.printscript.demo.shared.infrastructure.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Filtro perimetral que intercepta todas las peticiones HTTP entrantes con la máxima prioridad.
 *
 * Responsabilidades:
 * 1. Correlation ID: Lee "X-Correlation-Id" o genera un nuevo UUID.
 * 2. Carga inicial en MDC: Registra "correlation-id" y "service: snippet-service".
 * 3. Header de respuesta: Devuelve el "X-Correlation-Id" al cliente.
 * 4. Medición de latencia y log: Mide la duración total en ms y loguea el resultado (INFO/WARN/ERROR).
 * 5. Limpieza de hilo: Ejecuta MDC.clear() en `finally`.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-Id"
        const val CORRELATION_ID_KEY = "correlation-id"
        const val SERVICE_KEY = "service"
        const val SERVICE_NAME = "snippet-service"

        private const val HTTP_SERVER_ERROR_THRESHOLD = 500
        private const val HTTP_CLIENT_ERROR_THRESHOLD = 400
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val correlationId =
            request.getHeader(CORRELATION_ID_HEADER)?.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()

        MDC.put(CORRELATION_ID_KEY, correlationId)
        MDC.put(SERVICE_KEY, SERVICE_NAME)
        response.setHeader(CORRELATION_ID_HEADER, correlationId)

        val start = System.currentTimeMillis()
        try {
            filterChain.doFilter(request, response)
        } finally {
            val duration = System.currentTimeMillis() - start
            val status = response.status
            val method = request.method
            val uri = request.requestURI
            val message = "$method $uri - $status (${duration}ms)"

            when {
                status >= HTTP_SERVER_ERROR_THRESHOLD -> log.error(message)
                status >= HTTP_CLIENT_ERROR_THRESHOLD -> log.warn(message)
                else -> log.info(message)
            }

            MDC.clear()
        }
    }
}
