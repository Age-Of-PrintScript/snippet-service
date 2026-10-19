package age.of.printscript.demo.application

import age.of.printscript.demo.domain.Language
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.domain.ValidationError
import age.of.printscript.demo.domain.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreateSnippetTest {
    private val code = "let a: number = 5;"

    private fun createDto(
        language: String = "PrintScript",
        languageVersion: String = "1.1",
    ) = CreateSnippetDto(
        title = "My Snippet",
        description = "A test snippet",
        ownerId = UserId("alice"),
        language = language,
        languageVersion = languageVersion,
    )

    @Test
    fun `returns Created and persists snippet and content when validation succeeds`() {
        val repo = InMemorySnippetRepository()
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript", result = ValidationResult.Success)
        val createSnippet = CreateSnippet(repo, storage, listOf(language))
        val dto = createDto(language = "PrintScript")

        val result = createSnippet(dto, code)

        assertTrue(result is CreateSnippetResult.Created)
        val created = (result as CreateSnippetResult.Created).snippet
        assertEquals(1, created.version)
        assertEquals("My Snippet", created.title)
        assertEquals("A test snippet", created.description)
        assertEquals(UserId("alice"), created.ownerId)
        assertEquals("printscript", created.language)
        assertEquals("1.1", created.languageVersion)
        assertEquals(code, created.content)

        val persisted = requireNotNull(repo.findLatest(created.id))
        assertEquals(code, storage.read(persisted.contentUrl))
    }

    @Test
    fun `returns InvalidSnippet with line and column and deletes stored content when parser rejects code`() {
        val repo = InMemorySnippetRepository()
        val storage = FakeSnippetStorage()
        val errors = listOf(ValidationError("Missing semicolon"))
        val language = FakeLanguage(name = "printscript", result = ValidationResult.Failure(errors))
        val createSnippet = CreateSnippet(repo, storage, listOf(language))

        val result = createSnippet(createDto(), "let a: number = 5")

        assertEquals(CreateSnippetResult.InvalidSnippet(errors), result)
        assertTrue(repo.findAllByOwner(UserId("alice")).isEmpty())
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `returns UnsupportedLanguage without saving when language is not registered`() {
        val repo = InMemorySnippetRepository()
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript")
        val createSnippet = CreateSnippet(repo, storage, listOf(language))

        val result = createSnippet(createDto(language = "python"), code)

        assertEquals(CreateSnippetResult.UnsupportedLanguage("python"), result)
        assertTrue(repo.findAllByOwner(UserId("alice")).isEmpty())
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `returns UnsupportedVersion and deletes stored content when language version is not supported`() {
        val repo = InMemorySnippetRepository()
        val storage = FakeSnippetStorage()
        val language = FakeLanguage(name = "printscript", result = ValidationResult.UnsupportedVersion)
        val createSnippet = CreateSnippet(repo, storage, listOf(language))

        val result = createSnippet(createDto(languageVersion = "9.9"), code)

        assertEquals(CreateSnippetResult.UnsupportedVersion("9.9"), result)
        assertTrue(repo.findAllByOwner(UserId("alice")).isEmpty())
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `deletes stored content and rethrows when language service throws unexpected exception`() {
        val repo = InMemorySnippetRepository()
        val storage = FakeSnippetStorage()
        val failingLanguage =
            object : Language {
                override val name: String = "printscript"

                override fun validate(
                    code: String,
                    version: String,
                ): ValidationResult = error("Language service unavailable")
            }
        val createSnippet = CreateSnippet(repo, storage, listOf(failingLanguage))

        assertThrows(IllegalStateException::class.java) {
            createSnippet(createDto(), code)
        }

        assertTrue(repo.findAllByOwner(UserId("alice")).isEmpty())
        assertTrue(storage.isEmpty())
    }
}
