package age.of.printscript.demo.testing.infrastructure.controller

import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.application.testJwt
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.CreateSnippetTest
import age.of.printscript.demo.testing.application.FakeSnippetTestStorage
import age.of.printscript.demo.testing.application.InMemorySnippetTestRepository
import org.junit.jupiter.api.Assertions.assertEquals
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

    private fun buildController(repo: InMemorySnippetRepository = InMemorySnippetRepository()) =
        TestController(
            CreateSnippetTest(repo, InMemorySnippetTestRepository(), FakeSnippetTestStorage()),
        )
}
