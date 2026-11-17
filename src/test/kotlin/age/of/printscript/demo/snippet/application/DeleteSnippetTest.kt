package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.application.port.SnippetStorage
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DeleteSnippetTest {
    @Test
    fun `owner deletes snippet and all version blobs`() {
        val id = SnippetId("11111111-1111-1111-1111-111111111111")
        val v1 = createDummySnippet(id = id, ownerId = UserId("alice"), version = 1, contentUrl = "blob-v1")
        val v2 = createDummySnippet(id = id, ownerId = UserId("alice"), version = 2, contentUrl = "blob-v2")
        val repo = InMemorySnippetRepository(listOf(v1, v2))
        val storage =
            FakeSnippetStorage().apply {
                set("blob-v1", "v1")
                set("blob-v2", "v2")
            }

        val result = DeleteSnippet(repo, storage)(id, UserId("alice"))

        assertEquals(DeleteSnippetResult.Deleted, result)
        assertNull(repo.findLatest(id))
        assertTrue(repo.findAllVersions(id).isEmpty())
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `returns Forbidden and keeps snippet when requester is not owner`() {
        val id = SnippetId("11111111-1111-1111-1111-111111111111")
        val v1 = createDummySnippet(id = id, ownerId = UserId("alice"), version = 1, contentUrl = "blob-v1")
        val repo = InMemorySnippetRepository(listOf(v1))
        val storage = FakeSnippetStorage().apply { set("blob-v1", "v1") }

        val result = DeleteSnippet(repo, storage)(id, UserId("bob"))

        assertEquals(DeleteSnippetResult.Forbidden, result)
        assertEquals(1, repo.findAllVersions(id).size)
        assertEquals("v1", storage.read("blob-v1"))
    }

    @Test
    fun `returns NotFound when snippet does not exist`() {
        val result =
            DeleteSnippet(InMemorySnippetRepository(), FakeSnippetStorage())(SnippetId("missing"), UserId("alice"))
        assertEquals(DeleteSnippetResult.NotFound, result)
    }

    @Test
    fun `still deletes from repository when storage delete fails`() {
        val id = SnippetId("11111111-1111-1111-1111-111111111111")
        val v1 = createDummySnippet(id = id, ownerId = UserId("alice"), version = 1, contentUrl = "blob-v1")
        val repo = InMemorySnippetRepository(listOf(v1))
        val failingStorage =
            object : SnippetStorage {
                override fun read(url: String) = ""

                override fun save(
                    id: SnippetId,
                    version: Int,
                    content: String,
                ) = urlFor(id, version)

                override fun delete(url: String) = error("storage down")

                private fun urlFor(
                    id: SnippetId,
                    version: Int,
                ) = "${id.value}-$version"
            }

        val result = DeleteSnippet(repo, failingStorage)(id, UserId("alice"))

        assertEquals(DeleteSnippetResult.Deleted, result)
        assertTrue(repo.findAllVersions(id).isEmpty())
    }
}
