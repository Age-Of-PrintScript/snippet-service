package age.of.printscript.demo.infrastructure.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
}
