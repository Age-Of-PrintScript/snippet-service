package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.application.FakePermissionGateway
import age.of.printscript.demo.application.FakeSnippetStorage
import age.of.printscript.demo.application.GetSnippet
import age.of.printscript.demo.application.InMemorySnippetRepository
import age.of.printscript.demo.application.createDummySnippet
import age.of.printscript.demo.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class SnippetControllerTest {
    private val expectedCode = "let a: number = 5;"

    @Test
    fun `returns 200 OK with snippet when found`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val storage = FakeSnippetStorage().apply { set(snippet.contentUrl, expectedCode) }
        val permissions = FakePermissionGateway()
        val controller = SnippetController(GetSnippet(repo, storage, permissions))

        val response = controller.get(snippet.id.value, "alice", null)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(snippet.id.value, response.body?.id)
        assertEquals("alice", response.body?.ownerId)
        assertEquals(expectedCode, response.body?.content)
    }

    @Test
    fun `returns 404 NOT FOUND when snippet does not exist`() {
        val repo = InMemorySnippetRepository()
        val storage = FakeSnippetStorage()
        val permissions = FakePermissionGateway()
        val controller = SnippetController(GetSnippet(repo, storage, permissions))

        val response = controller.get("non-existent", "alice", null)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `returns 403 FORBIDDEN when user has no access`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val storage = FakeSnippetStorage()
        val permissions = FakePermissionGateway()
        val controller = SnippetController(GetSnippet(repo, storage, permissions))

        val response = controller.get(snippet.id.value, "bob", null)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
}
