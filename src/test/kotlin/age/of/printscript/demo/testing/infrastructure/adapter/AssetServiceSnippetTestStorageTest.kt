package age.of.printscript.demo.testing.infrastructure.adapter

import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class AssetServiceSnippetTestStorageTest {
    private val baseUrl = "http://localhost:8081"
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var storage: AssetServiceSnippetTestStorage

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder().baseUrl(baseUrl)
        mockServer = MockRestServiceServer.bindTo(builder).build()
        storage = AssetServiceSnippetTestStorage(baseUrl, builder.build())
    }

    @Test
    fun `save sends ordered inputs and outputs as JSON and returns asset url`() {
        val id = SnippetTestId("test-1")
        val content = SnippetTestContent(listOf("2", "3"), listOf("5"))
        val expectedUrl = "$baseUrl/v1/asset/snippet-tests/${id.value}"

        mockServer
            .expect(requestTo(expectedUrl))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""{"inputs":["2","3"],"outputs":["5"]}"""))
            .andRespond(withSuccess())

        val result = storage.save(id, content)

        mockServer.verify()
        assertEquals(expectedUrl, result)
    }

    @Test
    fun `read sends GET request and returns test content`() {
        val url = "$baseUrl/v1/asset/snippet-tests/test-1"

        mockServer
            .expect(requestTo(url))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""{"inputs":["2","3"],"outputs":["5"]}""", MediaType.APPLICATION_JSON))

        val result = storage.read(url)

        mockServer.verify()
        assertEquals(SnippetTestContent(listOf("2", "3"), listOf("5")), result)
    }

    @Test
    fun `delete sends DELETE request to test asset url`() {
        val url = "$baseUrl/v1/asset/snippet-tests/test-1"

        mockServer
            .expect(requestTo(url))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withNoContent())

        storage.delete(url)

        mockServer.verify()
    }
}
