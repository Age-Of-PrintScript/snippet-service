package age.of.printscript.demo.infrastructure.adapter

import age.of.printscript.demo.application.port.SnippetStorage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class AssetServiceSnippetStorage(
    @Value("\${services.asset.url}") private val baseUrl: String,
    private val assetRestClient: RestClient,
) : SnippetStorage {
    private val container = "snippets"

    override fun read(url: String): String =
        assetRestClient
            .get()
            .uri(url)
            .retrieve()
            .body<String>() ?: ""

    override fun save(
        key: String,
        content: String,
    ): String {
        assetRestClient
            .put()
            .uri("/v1/asset/{container}/{key}", container, key)
            .body(content)
            .retrieve()
            .toBodilessEntity()

        return "$baseUrl/v1/asset/$container/$key"
    }

    override fun delete(url: String) {
        assetRestClient
            .delete()
            .uri(url)
            .retrieve()
            .toBodilessEntity()
    }
}
