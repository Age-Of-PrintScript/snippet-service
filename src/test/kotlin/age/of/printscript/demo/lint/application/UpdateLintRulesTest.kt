package age.of.printscript.demo.lint.application

import age.of.printscript.demo.lint.domain.ActiveLintRule
import age.of.printscript.demo.lint.domain.LintRule
import age.of.printscript.demo.lint.domain.LintRuleId
import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatus
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpdateLintRulesTest {
    @Test
    fun `updates active rules, marks owner snippets as PENDING, and schedules lint batch`() {
        val alice = UserId("alice")
        val bob = UserId("bob")
        val aliceSnippet1 = createDummySnippet(id = SnippetId("s-1"), ownerId = alice, version = 1)
        val aliceSnippet2 = createDummySnippet(id = SnippetId("s-2"), ownerId = alice, version = 3)
        val bobSnippet = createDummySnippet(id = SnippetId("s-3"), ownerId = bob, version = 1)

        val snippetRepo = InMemorySnippetRepository(listOf(aliceSnippet1, aliceSnippet2, bobSnippet))
        val lintRules =
            listOf(
                LintRule(LintRuleId(1), "identifier-format", "camelCase"),
                LintRule(LintRuleId(2), "println-no-expression", null),
            )
        val lintRuleRepo = InMemoryLintRuleRepository(lintRules)
        val lintStatusRepo =
            InMemorySnippetLintStatusRepository(
                listOf(
                    SnippetLintStatus(aliceSnippet1.id, aliceSnippet1.version, LintStatus.COMPLIANT),
                    SnippetLintStatus(bobSnippet.id, bobSnippet.version, LintStatus.COMPLIANT),
                ),
            )
        val batchScheduler = FakeLintBatchScheduler()
        val updateLintRules = UpdateLintRules(lintRuleRepo, snippetRepo, lintStatusRepo, batchScheduler)

        val selections =
            listOf(
                RuleSelection(LintRuleId(1), "snake_case"),
                RuleSelection(LintRuleId(2), null),
            )
        val result = updateLintRules(alice, selections)

        assertTrue(result is UpdateLintRulesResult.Success)
        val success = result as UpdateLintRulesResult.Success
        val expectedActiveRules =
            listOf(
                ActiveLintRule(alice, LintRuleId(1), "snake_case"),
                ActiveLintRule(alice, LintRuleId(2), null),
            )
        assertEquals(expectedActiveRules, success.activeRules)
        assertEquals(expectedActiveRules, lintRuleRepo.findActiveByUser(alice))

        assertEquals(LintStatus.PENDING, lintStatusRepo.findByVersion(aliceSnippet1.id, aliceSnippet1.version)?.status)
        assertEquals(LintStatus.PENDING, lintStatusRepo.findByVersion(aliceSnippet2.id, aliceSnippet2.version)?.status)
        assertEquals(LintStatus.COMPLIANT, lintStatusRepo.findByVersion(bobSnippet.id, bobSnippet.version)?.status)

        assertEquals(1, batchScheduler.scheduledBatches.size)
        val scheduled = batchScheduler.scheduledBatches.first()
        assertEquals(alice, scheduled.userId)
        assertEquals(listOf(aliceSnippet1, aliceSnippet2), scheduled.snippets)
        assertEquals(
            mapOf(
                "identifier-format" to "snake_case",
                "println-no-expression" to null,
            ),
            scheduled.rules,
        )
    }

    @Test
    fun `falls back to rule defaultValue when selection value is null`() {
        val alice = UserId("alice")
        val snippet = createDummySnippet(ownerId = alice)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val lintRules = listOf(LintRule(LintRuleId(1), "identifier-format", "camelCase"))
        val lintRuleRepo = InMemoryLintRuleRepository(lintRules)
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val batchScheduler = FakeLintBatchScheduler()
        val updateLintRules = UpdateLintRules(lintRuleRepo, snippetRepo, lintStatusRepo, batchScheduler)

        updateLintRules(
            userId = alice,
            rules = listOf(RuleSelection(LintRuleId(1), null)),
        )

        val scheduled = batchScheduler.scheduledBatches.first()
        assertEquals(mapOf("identifier-format" to "camelCase"), scheduled.rules)
    }

    @Test
    fun `returns InvalidRules and does not modify state when unknown ruleId is provided`() {
        val alice = UserId("alice")
        val snippet = createDummySnippet(ownerId = alice)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val lintRules = listOf(LintRule(LintRuleId(1), "identifier-format", "camelCase"))
        val initialActive = listOf(ActiveLintRule(alice, LintRuleId(1), "camelCase"))
        val lintRuleRepo = InMemoryLintRuleRepository(lintRules, initialActive)
        val lintStatusRepo =
            InMemorySnippetLintStatusRepository(
                listOf(SnippetLintStatus(snippet.id, snippet.version, LintStatus.COMPLIANT)),
            )
        val batchScheduler = FakeLintBatchScheduler()
        val updateLintRules = UpdateLintRules(lintRuleRepo, snippetRepo, lintStatusRepo, batchScheduler)

        val selections =
            listOf(
                RuleSelection(LintRuleId(1), "snake_case"),
                RuleSelection(LintRuleId(99), null),
            )
        val result = updateLintRules(alice, selections)

        assertEquals(UpdateLintRulesResult.InvalidRules(listOf(99)), result)
        assertEquals(initialActive, lintRuleRepo.findActiveByUser(alice))
        assertEquals(LintStatus.COMPLIANT, lintStatusRepo.findByVersion(snippet.id, snippet.version)?.status)
        assertTrue(batchScheduler.scheduledBatches.isEmpty())
    }
}
