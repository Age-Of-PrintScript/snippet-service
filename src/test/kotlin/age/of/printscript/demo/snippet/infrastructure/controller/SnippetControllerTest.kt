package age.of.printscript.demo.snippet.infrastructure.controller

import age.of.printscript.demo.snippet.application.CreateSnippet
import age.of.printscript.demo.snippet.application.CreateSnippetTest
import age.of.printscript.demo.snippet.application.FakeLanguage
import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.FakeSnippetStorage
import age.of.printscript.demo.snippet.application.FakeSnippetTestStorage
import age.of.printscript.demo.snippet.application.GetSnippet
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.InMemorySnippetTestRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.application.testJwt
import age.of.printscript.demo.snippet.domain.Language
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.snippet.domain.ValidationError
import age.of.printscript.demo.snippet.domain.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

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
        createSnippetTest = createSnippetTest(repo),
    )

    private fun createSnippetTest(repo: InMemorySnippetRepository) =
        CreateSnippetTest(
            repo,
            InMemorySnippetTestRepository(),
            FakeSnippetTestStorage(),
        )

    @Test
    fun `returns 200 OK with snippet when found`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val storage = FakeSnippetStorage().apply { set(snippet.contentUrl, expectedCode) }
        val controller = buildController(repo = repo, storage = storage)

        val response = controller.get(snippet.id.value, testJwt("alice"), null)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(snippet.id.value, response.body?.id)
        assertEquals("alice", response.body?.ownerId)
        assertEquals(expectedCode, response.body?.content)
    }

    @Test
    fun `returns 404 NOT FOUND when snippet does not exist`() {
        val controller = buildController()

        val response = controller.get("non-existent", testJwt("alice"), null)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `returns 403 FORBIDDEN when user has no access`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val controller = buildController(repo = repo)

        val response = controller.get(snippet.id.value, testJwt("bob"), null)

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

        val response = controller.create(testJwt("alice"), request)

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

        val response = controller.create(testJwt("alice"), request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(errors, response.body)
    }

    @Test
    fun `createTest returns 201 CREATED with test summary when user owns snippet`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val controller = buildController(repo = InMemorySnippetRepository(listOf(snippet)))
        val request = CreateSnippetTestRequest(inputs = listOf("2", "3"), outputs = listOf("5"))

        val response = controller.createTest(snippet.id.value, "alice", request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(snippet.id.value, response.body?.snippetId)
        assertEquals(false, response.body?.id.isNullOrBlank())
        assertEquals(false, response.body?.createdAt == null)
    }

    @Test
    fun `createTest returns 404 NOT FOUND when snippet does not exist`() {
        val controller = buildController()

        val response = controller.createTest("unknown", "alice", CreateSnippetTestRequest(emptyList(), emptyList()))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `createTest returns 403 FORBIDDEN when user does not own snippet`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val controller = buildController(repo = InMemorySnippetRepository(listOf(snippet)))

        val response =
            controller.createTest(
                snippet.id.value,
                "bob",
                CreateSnippetTestRequest(emptyList(), emptyList()),
            )

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `createTest returns 400 BAD REQUEST for incomplete request body`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val controller = buildController(repo = InMemorySnippetRepository(listOf(snippet)))
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        mockMvc
            .perform(
                post("/snippet/${snippet.id.value}/test")
                    .header("X-User-Id", "alice")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"inputs":[]}"""),
            ).andExpect(status().isBadRequest)
    }
}
