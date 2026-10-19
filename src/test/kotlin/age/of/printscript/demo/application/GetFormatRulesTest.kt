package age.of.printscript.demo.application

import age.of.printscript.demo.domain.FormatRule
import age.of.printscript.demo.domain.FormatRuleId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetFormatRulesTest {
    @Test
    fun `returns empty list when there are no format rules`() {
        val repository = InMemoryFormatRuleRepository()
        val getFormatRules = GetFormatRules(repository)

        val result = getFormatRules()

        assertTrue(result.isEmpty())
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

        val result = getFormatRules()

        assertEquals(rules, result)
    }
}
