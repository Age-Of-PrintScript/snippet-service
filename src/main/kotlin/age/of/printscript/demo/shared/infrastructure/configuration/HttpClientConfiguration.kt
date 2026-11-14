package age.of.printscript.demo.shared.infrastructure.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.client.RestClient

@Configuration
class HttpClientConfiguration {
    @Bean
    fun assetRestClient(
        @Value("\${services.asset.url}") baseUrl: String,
        builder: RestClient.Builder,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .build()

    @Bean
    fun permissionRestClient(
        @Value("\${services.permissions.url}") baseUrl: String,
        builder: RestClient.Builder,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .requestInterceptor(bearerTokenForwarder())
            .build()

    @Bean
    fun printscriptRestClient(
        @Value("\${services.printscript.url}") baseUrl: String,
        builder: RestClient.Builder,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
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
}
