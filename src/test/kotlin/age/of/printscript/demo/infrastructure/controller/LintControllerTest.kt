package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.application.FakeJobPublisher
import age.of.printscript.demo.application.InMemoryLintRuleRepository
import age.of.printscript.demo.application.InMemorySnippetLintStatusRepository
import age.of.printscript.demo.application.InMemorySnippetRepository
import age.of.printscript.demo.application.LintSnippet
import age.of.printscript.demo.application.createDummySnippet
import age.of.printscript.demo.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class LintControllerTest {
    private fun buildController(
        snippetRepo: InMemorySnippetRepository = InMemorySnippetRepository(),
        lintRuleRepo: InMemoryLintRuleRepository = InMemoryLintRuleRepository(),
        lintStatusRepo: InMemorySnippetLintStatusRepository = InMemorySnippetLintStatusRepository(),
        jobPublisher: FakeJobPublisher = FakeJobPublisher(),
    ) = LintController(
        lintSnippet = LintSnippet(jobPublisher, snippetRepo, lintRuleRepo, lintStatusRepo),
    )

    @Test
    fun `returns 202 ACCEPTED with jobId when snippet exists and user is owner`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val controller = buildController(snippetRepo = snippetRepo)

        val response = controller.lint(snippet.id.value, "alice")

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertNotNull(response.body?.jobId)
    }

    @Test
    fun `returns 404 NOT FOUND when snippet does not exist`() {
        val controller = buildController()

        val response = controller.lint("non-existent", "alice")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `returns 403 FORBIDDEN when user is not the snippet owner`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val controller = buildController(snippetRepo = snippetRepo)

        val response = controller.lint(snippet.id.value, "bob")

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
}
