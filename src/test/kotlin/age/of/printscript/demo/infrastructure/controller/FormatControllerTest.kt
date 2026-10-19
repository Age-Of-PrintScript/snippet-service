package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.application.GetFormatRules
import age.of.printscript.demo.application.InMemoryFormatRuleRepository
import age.of.printscript.demo.domain.FormatRule
import age.of.printscript.demo.domain.FormatRuleId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class FormatControllerTest {
    @Test
    fun `returns 200 OK with list of format rules`() {
        val rules =
            listOf(
                FormatRule(FormatRuleId(1), "space-before-colon", defaultValue = null),
                FormatRule(FormatRuleId(2), "indent-inside-if", defaultValue = 4),
            )
        val repository = InMemoryFormatRuleRepository(initialRules = rules)
        val controller = FormatController(GetFormatRules(repository))

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
        val repository = InMemoryFormatRuleRepository()
        val controller = FormatController(GetFormatRules(repository))

        val response = controller.getRules()

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertEquals(0, body?.rules?.size)
    }
}
