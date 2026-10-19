package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.application.GetFormatRules
import age.of.printscript.demo.domain.FormatRule
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class FormatRuleResponse(
    val id: Int,
    val name: String,
    val defaultValue: Int?,
)

data class FormatRulesResponse(
    val rules: List<FormatRuleResponse>,
)

private fun FormatRule.toResponse() =
    FormatRuleResponse(
        id = id.value,
        name = name,
        defaultValue = defaultValue,
    )

@RestController
@RequestMapping("/format")
class FormatController(
    private val getFormatRules: GetFormatRules,
) {
    @GetMapping("/rules")
    fun getRules(): ResponseEntity<FormatRulesResponse> {
        val rules = getFormatRules().map { it.toResponse() }
        return ResponseEntity.ok(FormatRulesResponse(rules))
    }
}
