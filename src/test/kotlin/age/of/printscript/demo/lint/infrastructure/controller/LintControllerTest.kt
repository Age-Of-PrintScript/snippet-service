package age.of.printscript.demo.lint.infrastructure.controller

import age.of.printscript.demo.jobs.application.FakeJobPublisher
import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.lint.application.FakeLintBatchScheduler
import age.of.printscript.demo.lint.application.InMemoryLintRuleRepository
import age.of.printscript.demo.lint.application.InMemorySnippetLintStatusRepository
import age.of.printscript.demo.lint.application.LintSnippet
import age.of.printscript.demo.lint.application.UpdateLintRules
import age.of.printscript.demo.lint.domain.LintRule
import age.of.printscript.demo.lint.domain.LintRuleId
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class LintControllerTest {
    private fun buildController(
        snippetRepo: InMemorySnippetRepository = InMemorySnippetRepository(),
        lintRuleRepo: InMemoryLintRuleRepository = InMemoryLintRuleRepository(),
    ): LintController {
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val jobRepo = InMemoryJobRepository()
        val jobPublisher = FakeJobPublisher()
        val batchScheduler = FakeLintBatchScheduler()
        return LintController(
            lintSnippet = LintSnippet(jobPublisher, snippetRepo, lintRuleRepo, lintStatusRepo, jobRepo),
            updateLintRules = UpdateLintRules(lintRuleRepo, snippetRepo, lintStatusRepo, batchScheduler),
        )
    }

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

    @Test
    fun `updateRules returns 200 OK with active rules when ruleIds are valid`() {
        val rules =
            listOf(
                LintRule(LintRuleId(1), "identifier-format", "camelCase"),
                LintRule(LintRuleId(2), "println-no-expression", null),
            )
        val controller = buildController(lintRuleRepo = InMemoryLintRuleRepository(rules))

        val selections =
            listOf(
                LintRuleSelectionRequest(ruleId = 1, value = "snake_case"),
                LintRuleSelectionRequest(ruleId = 2, value = null),
            )
        val response = controller.updateRules("alice", UpdateLintRulesRequest(selections))

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body as ActiveLintRulesResponse
        assertEquals(
            listOf(
                ActiveLintRuleResponse(ruleId = 1, value = "snake_case"),
                ActiveLintRuleResponse(ruleId = 2, value = null),
            ),
            body.rules,
        )
    }

    @Test
    fun `updateRules returns 400 BAD REQUEST when unknown ruleId is provided`() {
        val rules = listOf(LintRule(LintRuleId(1), "identifier-format", "camelCase"))
        val controller = buildController(lintRuleRepo = InMemoryLintRuleRepository(rules))

        val selections = listOf(LintRuleSelectionRequest(ruleId = 99, value = null))
        val response = controller.updateRules("alice", UpdateLintRulesRequest(selections))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(mapOf("unknownRuleIds" to listOf(99)), response.body)
    }
}
