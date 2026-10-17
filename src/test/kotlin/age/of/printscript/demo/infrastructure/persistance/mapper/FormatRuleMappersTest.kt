package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.ActiveFormatRule
import age.of.printscript.demo.domain.FormatRule
import age.of.printscript.demo.domain.FormatRuleId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.ActiveFormatRuleEntity
import age.of.printscript.demo.infrastructure.persistance.entity.FormatRuleEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FormatRuleMappersTest {
    @Test
    fun `maps FormatRuleEntity to Domain FormatRule`() {
        val entity =
            FormatRuleEntity(
                id = 9,
                formatRule = "indent-inside-if",
                defaultValue = 4,
            )

        val domain = entity.toDomain()

        assertEquals(FormatRuleId(9), domain.id)
        assertEquals("indent-inside-if", domain.name)
        assertEquals(4, domain.defaultValue)
    }

    @Test
    fun `maps Domain FormatRule to FormatRuleEntity`() {
        val domain =
            FormatRule(
                id = FormatRuleId(1),
                name = "enforce-spacing-around-equals",
                defaultValue = null,
            )

        val entity = domain.toEntity()

        assertEquals(1, entity.id)
        assertEquals("enforce-spacing-around-equals", entity.formatRule)
        assertNull(entity.defaultValue)
    }

    @Test
    fun `maps ActiveFormatRuleEntity to Domain ActiveFormatRule`() {
        val entity =
            ActiveFormatRuleEntity(
                userId = "auth0|123",
                formatRuleId = 9,
                value = 2,
            )

        val domain = entity.toDomain()

        assertEquals(UserId("auth0|123"), domain.userId)
        assertEquals(FormatRuleId(9), domain.ruleId)
        assertEquals(2, domain.value)
    }

    @Test
    fun `maps Domain ActiveFormatRule to ActiveFormatRuleEntity`() {
        val domain =
            ActiveFormatRule(
                userId = UserId("auth0|123"),
                ruleId = FormatRuleId(1),
                value = null,
            )

        val entity = domain.toEntity()

        assertEquals("auth0|123", entity.userId)
        assertEquals(1, entity.formatRuleId)
        assertNull(entity.value)
    }
}
