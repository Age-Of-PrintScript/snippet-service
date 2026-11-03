package age.of.printscript.demo.format.infrastructure.controller

import age.of.printscript.demo.format.application.FormatSnippet
import age.of.printscript.demo.format.application.GetFormatRules
import age.of.printscript.demo.format.application.InMemoryFormatRuleRepository
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleId
import age.of.printscript.demo.jobs.application.FakeJobPublisher
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class FormatControllerTest {
    private fun buildController(
        snippetRepo: InMemorySnippetRepository = InMemorySnippetRepository(),
        formatRuleRepo: InMemoryFormatRuleRepository = InMemoryFormatRuleRepository(),
        jobPublisher: FakeJobPublisher = FakeJobPublisher(),
    ) = FormatController(
        formatSnippet = FormatSnippet(jobPublisher, formatRuleRepo, snippetRepo),
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
}
