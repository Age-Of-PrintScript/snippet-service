package age.of.printscript.demo.application

import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetSnippetTest {
    @Test
    fun `returns Success when snippet exists and user is owner`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val permissions = FakePermissionGateway()
        val getSnippet = GetSnippet(repo, permissions)

        val result = getSnippet(snippet.id, UserId("alice"))

        assertEquals(GetSnippetResult.Success(snippet), result)
    }

    @Test
    fun `returns Success when user is not owner but has read permission`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val allowedUsers = setOf(snippet.id to UserId("bob"))
        val permissions = FakePermissionGateway(allowedUsers = allowedUsers)
        val getSnippet = GetSnippet(repo, permissions)

        val result = getSnippet(snippet.id, UserId("bob"))

        assertEquals(GetSnippetResult.Success(snippet), result)
    }

    @Test
    fun `returns Forbidden when user is not owner and lacks permission`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val permissions = FakePermissionGateway()
        val getSnippet = GetSnippet(repo, permissions)

        val result = getSnippet(snippet.id, UserId("bob"))

        assertEquals(GetSnippetResult.Forbidden, result)
    }

    @Test
    fun `returns NotFound when snippet does not exist`() {
        val repo = InMemorySnippetRepository()
        val permissions = FakePermissionGateway()
        val getSnippet = GetSnippet(repo, permissions)

        val result = getSnippet(SnippetId("inexistente"), UserId("alice"))

        assertEquals(GetSnippetResult.NotFound, result)
    }

    @Test
    fun `returns specific version when version parameter is provided`() {
        val snippetV1 = createDummySnippet(version = 1)
        val snippetV2 = createDummySnippet(version = 2)
        val repo = InMemorySnippetRepository(listOf(snippetV1, snippetV2))
        val permissions = FakePermissionGateway()
        val getSnippet = GetSnippet(repo, permissions)

        val result = getSnippet(snippetV1.id, UserId("alice"), version = 1)

        assertEquals(GetSnippetResult.Success(snippetV1), result)
    }
}
