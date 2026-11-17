package age.of.printscript.demo.testing.infrastructure.controller

import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.application.testJwt
import age.of.printscript.demo.testing.application.FakeSnippetTestStorage
import age.of.printscript.demo.testing.application.InMemorySnippetTestRepository
import age.of.printscript.demo.testing.application.ListSnippetTestStatuses
import age.of.printscript.demo.testing.application.ListSnippetTests
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

class TestsQueryControllerTest {
    private val snippet = createDummySnippet()
    private val snippets = InMemorySnippetRepository(listOf(snippet))
    private val tests = InMemorySnippetTestRepository()
    private val storage = FakeSnippetTestStorage()
    private val permissions = FakePermissionGateway()
    private val controller =
        TestsQueryController(
            ListSnippetTests(snippets, tests, storage, permissions),
            ListSnippetTestStatuses(snippets, tests, permissions),
        )

    @Test
    fun `full listing includes inputs outputs and the latest status`() {
        val id = SnippetTestId(UUID.randomUUID().toString())
        val content = SnippetTestContent(listOf("input"), listOf("expected"))
        val url = storage.save(id, content)
        tests.save(SnippetTest(id, snippet.id, snippet.ownerId, url, SnippetTestStatus.PASSED))

        val response = controller.list(snippet.id.value, testJwt(snippet.ownerId.value))
        val item = (response.body as List<*>).single() as TestListItemResponse

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(id.value, item.id)
        assertEquals(content.inputs, item.inputs)
        assertEquals(content.outputs, item.outputs)
        assertEquals("PASSED", item.status)
    }

    @Test
    fun `polling returns ids and statuses without reading test content`() {
        val id = SnippetTestId(UUID.randomUUID().toString())
        tests.save(SnippetTest(id, snippet.id, snippet.ownerId, "unavailable-content", SnippetTestStatus.PENDING))

        val response = controller.statuses(snippet.id.value, testJwt(snippet.ownerId.value))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(listOf(TestStatusResponse(id.value, "PENDING")), response.body)
    }

    @Test
    fun `queries return 403 for a user without access`() {
        val jwt = testJwt("stranger")

        assertEquals(HttpStatus.FORBIDDEN, controller.list(snippet.id.value, jwt).statusCode)
        assertEquals(HttpStatus.FORBIDDEN, controller.statuses(snippet.id.value, jwt).statusCode)
    }

    @Test
    fun `queries return 404 for a missing snippet`() {
        val jwt = testJwt(snippet.ownerId.value)

        assertEquals(HttpStatus.NOT_FOUND, controller.list("missing", jwt).statusCode)
        assertEquals(HttpStatus.NOT_FOUND, controller.statuses("missing", jwt).statusCode)
    }
}
