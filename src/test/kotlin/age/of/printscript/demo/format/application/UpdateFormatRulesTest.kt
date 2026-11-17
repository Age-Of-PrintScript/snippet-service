package age.of.printscript.demo.format.application

import age.of.printscript.demo.format.domain.ActiveFormatRule
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleId
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpdateFormatRulesTest {
    @Test
    fun `updates active rules and schedules format batch for owner snippets`() {
        val alice = UserId("alice")
        val bob = UserId("bob")
        val aliceSnippet1 = createDummySnippet(id = SnippetId("s-1"), ownerId = alice, version = 1)
        val aliceSnippet2 = createDummySnippet(id = SnippetId("s-2"), ownerId = alice, version = 3)
        val bobSnippet = createDummySnippet(id = SnippetId("s-3"), ownerId = bob, version = 1)

        val snippetRepo = InMemorySnippetRepository(listOf(aliceSnippet1, aliceSnippet2, bobSnippet))
        val formatRules =
            listOf(
                FormatRule(FormatRuleId(9), "indent-inside-if", 4),
                FormatRule(FormatRuleId(1), "enforce-spacing-around-equals", null),
            )
        val formatRuleRepo = InMemoryFormatRuleRepository(formatRules)
        val batchScheduler = FakeFormatBatchScheduler()
        val updateFormatRules = UpdateFormatRules(formatRuleRepo, snippetRepo, batchScheduler)

        val selections =
            listOf(
                FormatRuleSelection(FormatRuleId(9), 2),
                FormatRuleSelection(FormatRuleId(1), null),
            )
        val result = updateFormatRules(alice, selections)

        assertTrue(result is UpdateFormatRulesResult.Success)
        val success = result as UpdateFormatRulesResult.Success
        val expectedActiveRules =
            listOf(
                ActiveFormatRule(alice, FormatRuleId(9), 2),
                ActiveFormatRule(alice, FormatRuleId(1), null),
            )
        assertEquals(expectedActiveRules, success.activeRules)
        assertEquals(expectedActiveRules, formatRuleRepo.findActiveByUser(alice))

        assertEquals(1, batchScheduler.scheduledBatches.size)
        val scheduled = batchScheduler.scheduledBatches.first()
        assertEquals(alice, scheduled.userId)
        assertEquals(listOf(aliceSnippet1, aliceSnippet2), scheduled.snippets)
        assertEquals(
            mapOf(
                "indent-inside-if" to "2",
                "enforce-spacing-around-equals" to null,
            ),
            scheduled.rules,
        )
    }

    @Test
    fun `falls back to rule defaultValue when selection value is null`() {
        val alice = UserId("alice")
        val snippet = createDummySnippet(ownerId = alice)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val formatRules = listOf(FormatRule(FormatRuleId(9), "indent-inside-if", 4))
        val formatRuleRepo = InMemoryFormatRuleRepository(formatRules)
        val batchScheduler = FakeFormatBatchScheduler()
        val updateFormatRules = UpdateFormatRules(formatRuleRepo, snippetRepo, batchScheduler)

        updateFormatRules(
            userId = alice,
            rules = listOf(FormatRuleSelection(FormatRuleId(9), null)),
        )

        val scheduled = batchScheduler.scheduledBatches.first()
        assertEquals(mapOf("indent-inside-if" to "4"), scheduled.rules)
    }

    @Test
    fun `returns InvalidRules and does not modify state when unknown ruleId is provided`() {
        val alice = UserId("alice")
        val snippet = createDummySnippet(ownerId = alice)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val formatRules = listOf(FormatRule(FormatRuleId(9), "indent-inside-if", 4))
        val initialActive = listOf(ActiveFormatRule(alice, FormatRuleId(9), 2))
        val formatRuleRepo = InMemoryFormatRuleRepository(formatRules, initialActive)
        val batchScheduler = FakeFormatBatchScheduler()
        val updateFormatRules = UpdateFormatRules(formatRuleRepo, snippetRepo, batchScheduler)

        val selections =
            listOf(
                FormatRuleSelection(FormatRuleId(9), 8),
                FormatRuleSelection(FormatRuleId(99), null),
            )
        val result = updateFormatRules(alice, selections)

        assertEquals(UpdateFormatRulesResult.InvalidRules(listOf(99)), result)
        assertEquals(initialActive, formatRuleRepo.findActiveByUser(alice))
        assertTrue(batchScheduler.scheduledBatches.isEmpty())
    }
}
