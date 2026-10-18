package age.of.printscript.demo.infrastructure.adapter

import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class PermissionServiceTest {
    private val baseUrl = "http://localhost:8082"
    private lateinit var restClient: RestClient
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var permissionService: PermissionService

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder().baseUrl(baseUrl)
        mockServer = MockRestServiceServer.bindTo(builder).build()
        restClient = builder.build()
        permissionService = PermissionService(restClient)
    }

    @Test
    fun `hasReadAccess returns true when response contains snippet read permission with auth0 user id`() {
        val snippetId = SnippetId("snippet-123")
        val userId = UserId("auth0|456")
        val expectedUrl = "$baseUrl/permissions?userId=auth0%7C456&snippetId=${snippetId.value}"
        val responseJson = """[{"id":"1","name":"snippet:read"}]"""

        mockServer
            .expect(requestTo(expectedUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        val hasAccess = permissionService.hasReadAccess(snippetId, userId)

        mockServer.verify()
        assertTrue(hasAccess)
    }

    @Test
    fun `hasReadAccess returns false when response does not contain snippet read permission`() {
        val snippetId = SnippetId("snippet-123")
        val userId = UserId("auth0|456")
        val expectedUrl = "$baseUrl/permissions?userId=auth0%7C456&snippetId=${snippetId.value}"
        val responseJson = """[{"id":"2","name":"snippet:write"}]"""

        mockServer
            .expect(requestTo(expectedUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        val hasAccess = permissionService.hasReadAccess(snippetId, userId)

        mockServer.verify()
        assertFalse(hasAccess)
    }

    @Test
    fun `hasReadAccess returns false when permissions list is empty`() {
        val snippetId = SnippetId("snippet-123")
        val userId = UserId("user-456")
        val expectedUrl = "$baseUrl/permissions?userId=${userId.value}&snippetId=${snippetId.value}"

        mockServer
            .expect(requestTo(expectedUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON))

        val hasAccess = permissionService.hasReadAccess(snippetId, userId)

        mockServer.verify()
        assertFalse(hasAccess)
    }
}
