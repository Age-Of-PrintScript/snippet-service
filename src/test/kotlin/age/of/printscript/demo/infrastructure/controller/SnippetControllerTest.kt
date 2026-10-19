package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.application.CreateSnippet
import age.of.printscript.demo.application.FakeLanguage
import age.of.printscript.demo.application.FakePermissionGateway
import age.of.printscript.demo.application.FakeSnippetStorage
import age.of.printscript.demo.application.GetSnippet
import age.of.printscript.demo.application.InMemorySnippetRepository
import age.of.printscript.demo.application.createDummySnippet
import age.of.printscript.demo.domain.Language
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.domain.ValidationError
import age.of.printscript.demo.domain.ValidationResult
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
}
