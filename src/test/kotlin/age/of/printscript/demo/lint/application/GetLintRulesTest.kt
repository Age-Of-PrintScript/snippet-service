package age.of.printscript.demo.lint.application

import age.of.printscript.demo.lint.domain.ActiveLintRule
import age.of.printscript.demo.lint.domain.LintRule
import age.of.printscript.demo.lint.domain.LintRuleId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetLintRulesTest {
    private val userId = UserId("alice")

    @Test
    fun `returns empty rules and active rules when there are none`() {
        val repository = InMemoryLintRuleRepository()
        val getLintRules = GetLintRules(repository)

        val result = getLintRules(userId)

        assertTrue(result.rules.isEmpty())
        assertTrue(result.activeRules.isEmpty())
    }

    @Test
    fun `returns all available lint rules`() {
        val rules =
            listOf(
                LintRule(LintRuleId(1), "identifier-format", "camelCase"),
                LintRule(LintRuleId(2), "println-no-expression", null),
            )
        val repository = InMemoryLintRuleRepository(initialRules = rules)
        val getLintRules = GetLintRules(repository)

        val result = getLintRules(userId)

        assertEquals(rules, result.rules)
    }

    @Test
    fun `returns only the requesting user's active rules`() {
        val rules = listOf(LintRule(LintRuleId(1), "identifier-format", "camelCase"))
        val myActiveRule = ActiveLintRule(userId, LintRuleId(1), value = "snake_case")
        val someoneElsesActiveRule = ActiveLintRule(UserId("bob"), LintRuleId(1), value = "PascalCase")
        val repository =
            InMemoryLintRuleRepository(
                initialRules = rules,
                initialActiveRules = listOf(myActiveRule, someoneElsesActiveRule),
            )
        val getLintRules = GetLintRules(repository)

        val result = getLintRules(userId)

        assertEquals(listOf(myActiveRule), result.activeRules)
    }
}
