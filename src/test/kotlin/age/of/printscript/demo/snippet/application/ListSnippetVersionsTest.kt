package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ListSnippetVersionsTest {
    private val snippetId = SnippetId("snippet-1")
    private val ownerId = UserId("alice")
    private val otherUser = UserId("bob")

    @Test
    fun `returns all versions sorted by version desc when user is owner`() {
        val v1 = createDummySnippet(id = snippetId, ownerId = ownerId, version = 1).copy(isLatest = false)
        val v2 = createDummySnippet(id = snippetId, ownerId = ownerId, version = 2).copy(isLatest = false)
        val v3 = createDummySnippet(id = snippetId, ownerId = ownerId, version = 3).copy(isLatest = true)
        val repo = InMemorySnippetRepository(listOf(v1, v2, v3))
        val permissions = FakePermissionGateway()
        val listSnippetVersions = ListSnippetVersions(repo, permissions)

        val result = listSnippetVersions(snippetId, ownerId)

        assertTrue(result is ListSnippetVersionsResult.Success)
        val versions = (result as ListSnippetVersionsResult.Success).versions
        assertEquals(3, versions.size)
        assertEquals(3, versions[0].version)
        assertEquals(2, versions[1].version)
        assertEquals(1, versions[2].version)
    }

    @Test
    fun `returns all versions when non-owner user has read access`() {
        val snippet = createDummySnippet(id = snippetId, ownerId = ownerId, version = 1)
        val repo = InMemorySnippetRepository(listOf(snippet))
        val permissions = FakePermissionGateway(allowedUsers = setOf(snippetId to otherUser))
        val listSnippetVersions = ListSnippetVersions(repo, permissions)

        val result = listSnippetVersions(snippetId, otherUser)

        assertTrue(result is ListSnippetVersionsResult.Success)
        val versions = (result as ListSnippetVersionsResult.Success).versions
        assertEquals(1, versions.size)
        assertEquals(1, versions[0].version)
    }

    @Test
    fun `returns Forbidden when non-owner user has no read access`() {
        val snippet = createDummySnippet(id = snippetId, ownerId = ownerId, version = 1)
        val repo = InMemorySnippetRepository(listOf(snippet))
        val permissions = FakePermissionGateway()
        val listSnippetVersions = ListSnippetVersions(repo, permissions)

        val result = listSnippetVersions(snippetId, otherUser)

        assertEquals(ListSnippetVersionsResult.Forbidden, result)
    }

    @Test
    fun `returns NotFound when snippet does not exist`() {
        val repo = InMemorySnippetRepository()
        val permissions = FakePermissionGateway()
        val listSnippetVersions = ListSnippetVersions(repo, permissions)

        val result = listSnippetVersions(SnippetId("unknown"), ownerId)

        assertEquals(ListSnippetVersionsResult.NotFound, result)
    }
}
