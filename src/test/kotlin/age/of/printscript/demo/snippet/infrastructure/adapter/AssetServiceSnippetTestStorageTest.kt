package age.of.printscript.demo.snippet.infrastructure.adapter

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetTest
import age.of.printscript.demo.snippet.domain.SnippetTestContent
import age.of.printscript.demo.snippet.domain.SnippetTestId
import age.of.printscript.demo.snippet.domain.UserId
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
        val test = createTest()
        val expectedUrl = "$baseUrl/v1/asset/snippet-tests/${test.id.value}"

        mockServer
            .expect(requestTo(expectedUrl))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""{"inputs":["2","3"],"outputs":["5"]}"""))
            .andRespond(withSuccess())

        val result = storage.save(test)

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

    private fun createTest() =
        SnippetTest(
            id = SnippetTestId("test-1"),
            snippetId = SnippetId("snippet-1"),
            ownerId = UserId("alice"),
            inputs = listOf("2", "3"),
            outputs = listOf("5"),
        )
}
