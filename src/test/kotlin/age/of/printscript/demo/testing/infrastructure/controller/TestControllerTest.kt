package age.of.printscript.demo.testing.infrastructure.controller

import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.application.testJwt
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.CreateSnippetTest
import age.of.printscript.demo.testing.application.DeleteSnippetTest
import age.of.printscript.demo.testing.application.FakeSnippetTestStorage
import age.of.printscript.demo.testing.application.InMemorySnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class TestControllerTest {
    @Test
    fun `createTest returns 201 CREATED with test summary when user owns snippet`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val controller = buildController(InMemorySnippetRepository(listOf(snippet)))

        val response =
            controller.createTest(
                snippet.id.value,
                testJwt("alice"),
                CreateSnippetTestRequest(inputs = listOf("2", "3"), outputs = listOf("5")),
            )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(snippet.id.value, response.body?.snippetId)
        assertEquals(false, response.body?.id.isNullOrBlank())
        assertEquals(false, response.body?.createdAt == null)
    }

    @Test
    fun `createTest with id updates existing test and returns 201 CREATED`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val testRepo = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val testId = SnippetTestId("test-1")
        val contentUrl = storage.save(testId, SnippetTestContent(listOf("1"), listOf("1")))
        testRepo.save(
            SnippetTest(
                id = testId,
                snippetId = snippet.id,
                ownerId = snippet.ownerId,
                contentUrl = contentUrl,
                status = SnippetTestStatus.PASSED,
            ),
        )
        val controller = buildController(repo = repo, testRepo = testRepo, storage = storage)

        val response =
            controller.createTest(
                snippet.id.value,
                testJwt("alice"),
                CreateSnippetTestRequest(
                    id = testId.value,
                    inputs = listOf("2", "3"),
                    outputs = listOf("5"),
                ),
            )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(testId.value, response.body?.id)
        assertEquals(snippet.id.value, response.body?.snippetId)
        assertEquals(SnippetTestStatus.NOT_RUN, testRepo.findById(testId)?.status)
    }

    @Test
    fun `createTest returns 404 NOT FOUND when snippet does not exist`() {
        val response =
            buildController().createTest(
                "unknown",
                testJwt("alice"),
                CreateSnippetTestRequest(emptyList(), emptyList()),
            )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `createTest returns 403 FORBIDDEN when user does not own snippet`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val response =
            buildController(InMemorySnippetRepository(listOf(snippet))).createTest(
                snippet.id.value,
                testJwt("bob"),
                CreateSnippetTestRequest(emptyList(), emptyList()),
            )

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `deleteTest returns 204 NO CONTENT when user owns snippet`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val testRepo = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val testId = SnippetTestId("test-1")
        val contentUrl = storage.save(testId, SnippetTestContent(listOf("1"), listOf("1")))
        testRepo.save(
            SnippetTest(
                id = testId,
                snippetId = snippet.id,
                ownerId = snippet.ownerId,
                contentUrl = contentUrl,
            ),
        )
        val controller = buildController(repo = repo, testRepo = testRepo, storage = storage)

        val response = controller.deleteTest(testId.value, testJwt("alice"))

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertNull(testRepo.findById(testId))
    }

    @Test
    fun `deleteTest returns 404 NOT FOUND when test does not exist`() {
        val controller = buildController()

        val response = controller.deleteTest("missing", testJwt("alice"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `deleteTest returns 403 FORBIDDEN when user does not own snippet`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val repo = InMemorySnippetRepository(listOf(snippet))
        val testRepo = InMemorySnippetTestRepository()
        val storage = FakeSnippetTestStorage()
        val testId = SnippetTestId("test-1")
        val contentUrl = storage.save(testId, SnippetTestContent(listOf("1"), listOf("1")))
        testRepo.save(
            SnippetTest(
                id = testId,
                snippetId = snippet.id,
                ownerId = snippet.ownerId,
                contentUrl = contentUrl,
            ),
        )
        val controller = buildController(repo = repo, testRepo = testRepo, storage = storage)

        val response = controller.deleteTest(testId.value, testJwt("bob"))

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    private fun buildController(
        repo: InMemorySnippetRepository = InMemorySnippetRepository(),
        testRepo: InMemorySnippetTestRepository = InMemorySnippetTestRepository(),
        storage: FakeSnippetTestStorage = FakeSnippetTestStorage(),
    ) = TestController(
        CreateSnippetTest(repo, testRepo, storage),
        DeleteSnippetTest(repo, testRepo, storage),
    )
}
