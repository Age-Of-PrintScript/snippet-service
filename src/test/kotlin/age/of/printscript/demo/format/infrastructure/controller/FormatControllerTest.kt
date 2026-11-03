package age.of.printscript.demo.format.infrastructure.controller

import age.of.printscript.demo.format.application.FormatSnippet
import age.of.printscript.demo.format.application.GetFormatRules
import age.of.printscript.demo.format.application.InMemoryFormatRuleRepository
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleId
import age.of.printscript.demo.jobs.application.FakeJobPublisher
import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class FormatControllerTest {
    private fun buildController(
        snippetRepo: InMemorySnippetRepository = InMemorySnippetRepository(),
        formatRuleRepo: InMemoryFormatRuleRepository = InMemoryFormatRuleRepository(),
        jobRepo: InMemoryJobRepository = InMemoryJobRepository(),
        jobPublisher: FakeJobPublisher = FakeJobPublisher(),
    ) = FormatController(
        formatSnippet = FormatSnippet(jobPublisher, formatRuleRepo, snippetRepo, jobRepo),
        getFormatRules = GetFormatRules(formatRuleRepo),
    )

    @Test
    fun `returns 200 OK with list of format rules`() {
        val rules =
            listOf(
                FormatRule(FormatRuleId(1), "space-before-colon", defaultValue = null),
                FormatRule(FormatRuleId(2), "indent-inside-if", defaultValue = 4),
            )
        val controller =
            buildController(
                formatRuleRepo = InMemoryFormatRuleRepository(rules),
            )

        val response = controller.getRules()

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertEquals(2, body?.rules?.size)
        assertEquals(1, body?.rules?.first()?.id)
        assertEquals("space-before-colon", body?.rules?.first()?.name)
        assertEquals(null, body?.rules?.first()?.defaultValue)
    }

    @Test
    fun `returns 200 OK with empty list when no format rules exist`() {
        val controller = buildController()

        val response = controller.getRules()

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertEquals(0, body?.rules?.size)
    }

    @Test
    fun `returns 202 ACCEPTED with jobId when snippet exists and user is owner`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val controller = buildController(snippetRepo = snippetRepo)

        val response = controller.format(snippet.id.value, "alice")

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertNotNull(response.body?.jobId)
    }

    @Test
    fun `returns 404 NOT FOUND when snippet does not exist`() {
        val controller = buildController()

        val response = controller.format("non-existent", "alice")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `returns 403 FORBIDDEN when user is not the snippet owner`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val controller = buildController(snippetRepo = snippetRepo)

        val response = controller.format(snippet.id.value, "bob")

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
}
