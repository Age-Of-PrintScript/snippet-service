package age.of.printscript.demo.infrastructure.adapter

import age.of.printscript.demo.domain.SnippetId
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

class AssetServiceSnippetStorageTest {
    private val baseUrl = "http://localhost:8081"
    private lateinit var restClient: RestClient
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var storage: AssetServiceSnippetStorage

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder().baseUrl(baseUrl)
        mockServer = MockRestServiceServer.bindTo(builder).build()
        restClient = builder.build()
        storage = AssetServiceSnippetStorage(baseUrl, restClient)
    }

    @Test
    fun `save sends PUT request with content and returns asset url`() {
        val id = SnippetId("my-snippet")
        val version = 1
        val expectedKey = "my-snippet-1"
        val code = "let a: number = 42;"

        mockServer
            .expect(requestTo("$baseUrl/v1/asset/snippets/$expectedKey"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().string(code))
            .andRespond(withSuccess())

        val resultUrl = storage.save(id, version, code)

        mockServer.verify()
        assertEquals("$baseUrl/v1/asset/snippets/$expectedKey", resultUrl)
    }

    @Test
    fun `read sends GET request to url and returns content string`() {
        val url = "$baseUrl/v1/asset/snippets/my-snippet-1.ps"
        val code = "println(\"Hello world\");"

        mockServer
            .expect(requestTo(url))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(code, MediaType.TEXT_PLAIN))

        val result = storage.read(url)

        mockServer.verify()
        assertEquals(code, result)
    }

    @Test
    fun `delete sends DELETE request to url`() {
        val url = "$baseUrl/v1/asset/snippets/my-snippet-1.ps"

        mockServer
            .expect(requestTo(url))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withNoContent())

        storage.delete(url)

        mockServer.verify()
    }
}
