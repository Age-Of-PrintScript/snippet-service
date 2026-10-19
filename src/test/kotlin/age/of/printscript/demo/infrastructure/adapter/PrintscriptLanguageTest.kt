package age.of.printscript.demo.infrastructure.adapter

import age.of.printscript.demo.domain.ValidationError
import age.of.printscript.demo.domain.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest
import org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class PrintscriptLanguageTest {
    private val baseUrl = "http://localhost:8082"
    private lateinit var restClient: RestClient
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var language: PrintscriptLanguage

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder().baseUrl(baseUrl)
        mockServer = MockRestServiceServer.bindTo(builder).build()
        restClient = builder.build()
        language = PrintscriptLanguage(restClient)
    }

    @Test
    fun `validate returns Success when script is valid and service responds 204 No Content`() {
        val code = "let a: number = 5;"
        val version = "1.1"

        mockServer
            .expect(requestTo("$baseUrl/validate/new"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""{"code":"$code","version":"$version"}"""))
            .andRespond(withNoContent())

        val result = language.validate(code, version)

        mockServer.verify()
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validate returns Failure with errors when script is invalid and service responds 200 OK`() {
        val code = "let a: number = ;"
        val version = "1.1"
        val responseJson =
            """
            {
                "errors": [
                    { "message": "Unexpected token at line 1, column 17" }
                ]
            }
            """.trimIndent()

        mockServer
            .expect(requestTo("$baseUrl/validate/new"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""{"code":"$code","version":"$version"}"""))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        val result = language.validate(code, version)

        mockServer.verify()
        assertTrue(result is ValidationResult.Failure)
        val failure = result as ValidationResult.Failure
        assertEquals(listOf(ValidationError("Unexpected token at line 1, column 17")), failure.errors)
    }

    @Test
    fun `validate returns UnsupportedVersion when service responds 400 Bad Request`() {
        val code = "let a: number = 5;"
        val version = "2.0"
        val responseJson = """{"message":"version must be 1.0 or 1.1"}"""

        mockServer
            .expect(requestTo("$baseUrl/validate/new"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""{"code":"$code","version":"$version"}"""))
            .andRespond(withBadRequest().body(responseJson).contentType(MediaType.APPLICATION_JSON))

        val result = language.validate(code, version)

        mockServer.verify()
        assertEquals(ValidationResult.UnsupportedVersion, result)
    }

    @Test
    fun `validate throws RuntimeException when service responds 500 Internal Server Error`() {
        val code = "let a: number = 5;"
        val version = "1.1"

        mockServer
            .expect(requestTo("$baseUrl/validate/new"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""{"code":"$code","version":"$version"}"""))
            .andRespond(withServerError())

        assertThrows(RuntimeException::class.java) {
            language.validate(code, version)
        }

        mockServer.verify()
    }
}
