package age.of.printscript.demo.snippet.infrastructure.adapter

import age.of.printscript.demo.snippet.application.port.SnippetTestStorage
import age.of.printscript.demo.snippet.domain.SnippetTest
import age.of.printscript.demo.snippet.domain.SnippetTestContent
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class AssetServiceSnippetTestStorage(
    @Value("\${services.asset.url}") private val baseUrl: String,
    private val assetRestClient: RestClient,
) : SnippetTestStorage {
    override fun read(url: String): SnippetTestContent =
        assetRestClient
            .get()
            .uri(url)
            .retrieve()
            .body<SnippetTestContent>() ?: error("Test asset is empty")

    override fun save(test: SnippetTest): String {
        val key = test.id.value
        assetRestClient
            .put()
            .uri("/v1/asset/{container}/{key}", CONTAINER, key)
            .contentType(MediaType.APPLICATION_JSON)
            .body(SnippetTestContent(test.inputs, test.outputs))
            .retrieve()
            .toBodilessEntity()

        return "$baseUrl/v1/asset/$CONTAINER/$key"
    }

    override fun delete(url: String) {
        assetRestClient
            .delete()
            .uri(url)
            .retrieve()
            .toBodilessEntity()
    }

    private companion object {
        const val CONTAINER = "snippet-tests"
    }
}
