package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.ActiveLintRule
import age.of.printscript.demo.domain.LintRule
import age.of.printscript.demo.domain.LintRuleId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.ActiveLintRuleEntity
import age.of.printscript.demo.infrastructure.persistance.entity.LintRuleEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class LintRuleMappersTest {
    @Test
    fun `maps LintRuleEntity to Domain LintRule`() {
        val entity =
            LintRuleEntity(
                id = 1,
                lintRule = "identifier-format",
                defaultValue = "camelCase",
            )

        val domain = entity.toDomain()

        assertEquals(LintRuleId(1), domain.id)
        assertEquals("identifier-format", domain.name)
        assertEquals("camelCase", domain.defaultValue)
    }

    @Test
    fun `maps Domain LintRule to LintRuleEntity`() {
        val domain =
            LintRule(
                id = LintRuleId(2),
                name = "println-no-expression",
                defaultValue = null,
            )

        val entity = domain.toEntity()

        assertEquals(2, entity.id)
        assertEquals("println-no-expression", entity.lintRule)
        assertNull(entity.defaultValue)
    }

    @Test
    fun `maps ActiveLintRuleEntity to Domain ActiveLintRule`() {
        val entity =
            ActiveLintRuleEntity(
                userId = "auth0|123",
                lintRuleId = 1,
                value = "snake_case",
            )

        val domain = entity.toDomain()

        assertEquals(UserId("auth0|123"), domain.userId)
        assertEquals(LintRuleId(1), domain.ruleId)
        assertEquals("snake_case", domain.value)
    }

    @Test
    fun `maps Domain ActiveLintRule to ActiveLintRuleEntity`() {
        val domain =
            ActiveLintRule(
                userId = UserId("auth0|123"),
                ruleId = LintRuleId(2),
                value = null,
            )

        val entity = domain.toEntity()

        assertEquals("auth0|123", entity.userId)
        assertEquals(2, entity.lintRuleId)
        assertNull(entity.value)
    }
}
