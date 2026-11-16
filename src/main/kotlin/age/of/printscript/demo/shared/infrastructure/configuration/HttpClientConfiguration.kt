package age.of.printscript.demo.shared.infrastructure.configuration

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.client.RestClient
import java.io.IOException

@Configuration
class HttpClientConfiguration {
    companion object {
        private const val HTTP_SERVER_ERROR_THRESHOLD = 500
        private const val HTTP_CLIENT_ERROR_THRESHOLD = 400
        private const val CORRELATION_ID_HEADER = "X-Correlation-Id"
        private const val CORRELATION_ID_KEY = "correlation-id"
    }

    @Bean
    fun assetRestClient(
        @Value("\${services.asset.url}") baseUrl: String,
        builder: RestClient.Builder,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .requestInterceptor(loggingAndCorrelationInterceptor())
            .build()

    @Bean
    fun permissionRestClient(
        @Value("\${services.permissions.url}") baseUrl: String,
        builder: RestClient.Builder,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .requestInterceptor(bearerTokenForwarder())
            .requestInterceptor(loggingAndCorrelationInterceptor())
            .build()

    @Bean
    fun printscriptRestClient(
        @Value("\${services.printscript.url}") baseUrl: String,
        builder: RestClient.Builder,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .requestInterceptor(loggingAndCorrelationInterceptor())
            .build()

    /** Reenvía el JWT del usuario autenticado a permissions-service, que también es resource server. */
    private fun bearerTokenForwarder() =
        ClientHttpRequestInterceptor { request, body, execution ->
            val jwt = SecurityContextHolder.getContext().authentication?.principal as? Jwt
            if (jwt != null) {
                request.headers.setBearerAuth(jwt.tokenValue)
            }
            execution.execute(request, body)
        }

    /**
     * Interceptor saliente para llamadas HTTP entre microservicios:
     * 1. Propaga el correlation-id desde el MDC en el header X-Correlation-Id.
     * 2. Mide la latencia y loguea el resultado sin incluir tokens ni secrets.
     */
    private fun loggingAndCorrelationInterceptor(): ClientHttpRequestInterceptor {
        val log = LoggerFactory.getLogger("ExternalHttpClient")
        return ClientHttpRequestInterceptor { request, body, execution ->
            val correlationId = MDC.get(CORRELATION_ID_KEY)
            if (!correlationId.isNullOrBlank()) {
                request.headers.set(CORRELATION_ID_HEADER, correlationId)
            }

            val start = System.currentTimeMillis()
            try {
                val response = execution.execute(request, body)
                val duration = System.currentTimeMillis() - start
                val status = response.statusCode.value()
                val message = "External call ${request.method} ${request.uri} - $status (${duration}ms)"

                when {
                    status >= HTTP_SERVER_ERROR_THRESHOLD -> log.error(message)
                    status >= HTTP_CLIENT_ERROR_THRESHOLD -> log.warn(message)
                    else -> log.info(message)
                }
                response
            } catch (ex: IOException) {
                val duration = System.currentTimeMillis() - start
                log.error(
                    "External call ${request.method} ${request.uri} failed after ${duration}ms: ${ex.message}",
                    ex,
                )
                throw ex
            }
        }
    }
}
