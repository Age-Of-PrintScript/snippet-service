package age.of.printscript.demo.snippet.infrastructure.controller

import age.of.printscript.demo.snippet.application.CreateSnippet
import age.of.printscript.demo.snippet.application.FakeLanguage
import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.FakeSnippetStorage
import age.of.printscript.demo.snippet.application.GetSnippet
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.UpdateSnippet
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.Language
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.snippet.domain.ValidationError
import age.of.printscript.demo.snippet.domain.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class SnippetControllerTest {
    private val expectedCode = "let a: number = 5;"

    private fun buildController(
        repo: InMemorySnippetRepository = InMemorySnippetRepository(),
        storage: FakeSnippetStorage = FakeSnippetStorage(),
        permissions: FakePermissionGateway = FakePermissionGateway(),
        languages: List<Language> = listOf(FakeLanguage()),
    ) = SnippetController(
        getSnippet = GetSnippet(repo, storage, permissions),
        createSnippet = CreateSnippet(repo, storage, languages),
        updateSnippet = UpdateSnippet(repo, storage, languages),
    )

    @Test
    fun `returns 200 OK with snippet when found`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val storage = FakeSnippetStorage().apply { set(snippet.contentUrl, expectedCode) }
        val controller = buildController(repo = repo, storage = storage)

        val response = controller.get(snippet.id.value, "alice", null)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(snippet.id.value, response.body?.id)
        assertEquals("alice", response.body?.ownerId)
        assertEquals(expectedCode, response.body?.content)
    }

    @Test
    fun `returns 404 NOT FOUND when snippet does not exist`() {
        val controller = buildController()

        val response = controller.get("non-existent", "alice", null)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `returns 403 FORBIDDEN when user has no access`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val controller = buildController(repo = repo)

        val response = controller.get(snippet.id.value, "bob", null)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `create returns 201 CREATED with SnippetResponse when snippet is valid`() {
        val controller = buildController()
        val request =
            CreateSnippetRequest(
                title = "Hello",
                description = "World",
                language = "PrintScript",
                languageVersion = "1.1",
                content = expectedCode,
            )

        val response = controller.create("alice", request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        val body = response.body as SnippetResponse
        assertEquals("Hello", body.title)
        assertEquals("alice", body.ownerId)
        assertEquals("printscript", body.language)
        assertEquals(expectedCode, body.content)
    }

    @Test
    fun `create returns 400 BAD REQUEST with validation errors when code is invalid`() {
        val errors = listOf(ValidationError("Missing semicolon"))
        val language = FakeLanguage(result = ValidationResult.Failure(errors))
        val controller = buildController(languages = listOf(language))
        val request =
            CreateSnippetRequest(
                title = "Broken",
                description = null,
                language = "printscript",
                languageVersion = "1.1",
                content = "let a: number = 5",
            )

        val response = controller.create("alice", request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(errors, response.body)
    }

    @Test
    fun `update returns 200 OK with updated SnippetResponse when snippet is valid`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(snippet))
        val controller = buildController(repo = repo)
        val updatedCode = "let b: number = 10;"

        val response = controller.update(snippet.id.value, "alice", UpdateSnippetRequest(updatedCode))

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body as SnippetResponse
        assertEquals(snippet.id.value, body.id)
        assertEquals(2, body.version)
        assertEquals(updatedCode, body.content)
    }

    @Test
    fun `update returns 404 NOT FOUND when snippet does not exist`() {
        val controller = buildController()

        val response = controller.update("non-existent", "alice", UpdateSnippetRequest(expectedCode))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `update returns 403 FORBIDDEN when user is not the owner`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(snippet))
        val controller = buildController(repo = repo)

        val response = controller.update(snippet.id.value, "bob", UpdateSnippetRequest(expectedCode))

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `update returns 400 BAD REQUEST with validation errors when code is invalid`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"), version = 1)
        val repo = InMemorySnippetRepository(listOf(snippet))
        val errors = listOf(ValidationError("Missing semicolon"))
        val language = FakeLanguage(result = ValidationResult.Failure(errors))
        val controller = buildController(repo = repo, languages = listOf(language))

        val response = controller.update(snippet.id.value, "alice", UpdateSnippetRequest("let a: number = 5"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(errors, response.body)
    }

    @Test
    fun `update returns 400 BAD REQUEST when language is unsupported`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"), version = 1).copy(language = "python")
        val repo = InMemorySnippetRepository(listOf(snippet))
        val controller = buildController(repo = repo)

        val response = controller.update(snippet.id.value, "alice", UpdateSnippetRequest(expectedCode))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(mapOf("error" to "Unsupported language: python"), response.body)
    }

    @Test
    fun `update returns 400 BAD REQUEST when language version is unsupported`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"), version = 1).copy(languageVersion = "9.9")
        val repo = InMemorySnippetRepository(listOf(snippet))
        val language = FakeLanguage(result = ValidationResult.UnsupportedVersion)
        val controller = buildController(repo = repo, languages = listOf(language))

        val response = controller.update(snippet.id.value, "alice", UpdateSnippetRequest(expectedCode))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(mapOf("error" to "Unsupported version: 9.9"), response.body)
    }
}
