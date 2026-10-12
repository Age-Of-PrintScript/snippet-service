package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.application.FakePermissionGateway
import age.of.printscript.demo.application.GetSnippet
import age.of.printscript.demo.application.InMemorySnippetRepository
import age.of.printscript.demo.application.createDummySnippet
import age.of.printscript.demo.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class SnippetControllerTest {
    @Test
    fun `returns 200 OK with snippet when found`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val permissions = FakePermissionGateway()
        val controller = SnippetController(GetSnippet(repo, permissions))

        val response = controller.get(snippet.id.value, "alice", null)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(snippet.id.value, response.body?.id)
        assertEquals("alice", response.body?.ownerId)
    }

    @Test
    fun `returns 404 NOT FOUND when snippet does not exist`() {
        val repo = InMemorySnippetRepository()
        val permissions = FakePermissionGateway()
        val controller = SnippetController(GetSnippet(repo, permissions))

        val response = controller.get("non-existent", "alice", null)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `returns 403 FORBIDDEN when user has no access`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val permissions = FakePermissionGateway()
        val controller = SnippetController(GetSnippet(repo, permissions))

        val response = controller.get(snippet.id.value, "bob", null)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
}
