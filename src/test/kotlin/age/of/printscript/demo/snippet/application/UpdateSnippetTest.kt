package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.snippet.domain.ValidationError
import age.of.printscript.demo.snippet.domain.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpdateSnippetTest {
    private val initialCode = "let a: number = 5;"
    private val updatedCode = "let a: number = 10;\nprintln(a);"

    @Test
    fun `returns Updated and persists new version as latest while marking previous version as not latest`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage().apply { set(existing.contentUrl, initialCode) }
        val language = FakeLanguage(name = "printscript", result = ValidationResult.Success)
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(existing.id, UserId("alice"), updatedCode)

        assertTrue(result is UpdateSnippetResult.Updated)
        val updated = (result as UpdateSnippetResult.Updated).snippet
        assertEquals(existing.id, updated.id)
        assertEquals(2, updated.version)
        assertEquals(existing.title, updated.title)
        assertEquals(existing.description, updated.description)
        assertEquals(UserId("alice"), updated.ownerId)
        assertEquals(existing.language, updated.language)
        assertEquals(existing.languageVersion, updated.languageVersion)
        assertEquals(updatedCode, updated.content)
        assertTrue(updated.isLatest)

        val latestPersisted = requireNotNull(repo.findLatest(existing.id))
        assertEquals(2, latestPersisted.version)
        assertTrue(latestPersisted.isLatest)
        assertEquals(updatedCode, storage.read(latestPersisted.contentUrl))

        val previousVersion = requireNotNull(repo.findByVersion(existing.id, 1))
        assertFalse(previousVersion.isLatest)
        assertEquals(initialCode, storage.read(previousVersion.contentUrl))
    }

    @Test
    fun `returns NotFound when snippet does not exist`() {
        val repo = InMemorySnippetRepository()
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript")
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(SnippetId("non-existent"), UserId("alice"), updatedCode)

        assertEquals(UpdateSnippetResult.NotFound, result)
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `returns Forbidden when user is not the owner`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript")
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(existing.id, UserId("bob"), updatedCode)

        assertEquals(UpdateSnippetResult.Forbidden, result)
        assertTrue(storage.isEmpty())
        assertEquals(1, repo.findLatest(existing.id)?.version)
    }

    @Test
    fun `returns InvalidSnippet without saving to storage when parser rejects code`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val errors = listOf(ValidationError("Missing semicolon"))
        val language = FakeLanguage(name = "printscript", result = ValidationResult.Failure(errors))
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(existing.id, UserId("alice"), "let a: number = 5")

        assertEquals(UpdateSnippetResult.InvalidSnippet(errors), result)
        assertTrue(storage.isEmpty())
        assertEquals(1, repo.findLatest(existing.id)?.version)
    }

    @Test
    fun `returns UnsupportedLanguage without saving when snippet language is not registered`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1).copy(language = "python")
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript")
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(existing.id, UserId("alice"), updatedCode)

        assertEquals(UpdateSnippetResult.UnsupportedLanguage("python"), result)
        assertTrue(storage.isEmpty())
        assertEquals(1, repo.findLatest(existing.id)?.version)
    }

    @Test
    fun `returns UnsupportedVersion without saving when language version is not supported`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1).copy(languageVersion = "9.9")
        val repo = InMemorySnippetRepository(listOf(existing))
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript", result = ValidationResult.UnsupportedVersion)
        val updateSnippet = UpdateSnippet(repo, storage, listOf(language))

        val result = updateSnippet(existing.id, UserId("alice"), updatedCode)

        assertEquals(UpdateSnippetResult.UnsupportedVersion("9.9"), result)
        assertTrue(storage.isEmpty())
        assertEquals(1, repo.findLatest(existing.id)?.version)
    }

    @Test
    fun `deletes stored content and rethrows when repository throws unexpected exception`() {
        val existing = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val failingRepo =
            object : SnippetRepository {
                override fun findLatest(id: SnippetId): Snippet? = existing

                override fun findByVersion(
                    id: SnippetId,
                    version: Int,
                ): Snippet? = null

                override fun findAllByOwner(ownerId: UserId): List<Snippet> = emptyList()

                override fun save(snippet: Snippet): Snippet = error("Database unavailable")
            }
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript", result = ValidationResult.Success)
        val updateSnippet = UpdateSnippet(failingRepo, storage, listOf(language))

        assertThrows(IllegalStateException::class.java) {
            updateSnippet(existing.id, UserId("alice"), updatedCode)
        }

        assertTrue(storage.isEmpty())
    }
}
