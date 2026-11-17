package age.of.printscript.demo.format.application

import age.of.printscript.demo.format.domain.ActiveFormatRule
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetFormatRulesTest {
    private val userId = UserId("alice")

    @Test
    fun `returns empty rules and active rules when there are none`() {
        val repository = InMemoryFormatRuleRepository()
        val getFormatRules = GetFormatRules(repository)

        val result = getFormatRules(userId)

        assertTrue(result.rules.isEmpty())
        assertTrue(result.activeRules.isEmpty())
    }

    @Test
    fun `returns all available format rules`() {
        val rules =
            listOf(
                FormatRule(FormatRuleId(1), "space-before-colon", defaultValue = null),
                FormatRule(FormatRuleId(2), "indent-inside-if", defaultValue = 4),
            )
        val repository = InMemoryFormatRuleRepository(initialRules = rules)
        val getFormatRules = GetFormatRules(repository)

        val result = getFormatRules(userId)

        assertEquals(rules, result.rules)
    }

    @Test
    fun `returns only the requesting user's active rules`() {
        val rules = listOf(FormatRule(FormatRuleId(1), "indent-inside-if", defaultValue = 4))
        val myActiveRule = ActiveFormatRule(userId, FormatRuleId(1), value = 2)
        val someoneElsesActiveRule = ActiveFormatRule(UserId("bob"), FormatRuleId(1), value = 8)
        val repository =
            InMemoryFormatRuleRepository(
                initialRules = rules,
                initialActiveRules = listOf(myActiveRule, someoneElsesActiveRule),
            )
        val getFormatRules = GetFormatRules(repository)

        val result = getFormatRules(userId)

        assertEquals(listOf(myActiveRule), result.activeRules)
    }
}
