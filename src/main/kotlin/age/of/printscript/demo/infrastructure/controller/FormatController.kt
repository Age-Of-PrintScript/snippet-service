package age.of.printscript.demo.infrastructure.controller

import age.of.printscript.demo.application.FormatSnippet
import age.of.printscript.demo.application.FormatSnippetResult
import age.of.printscript.demo.application.GetFormatRules
import age.of.printscript.demo.domain.FormatRule
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
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

data class FormatJobAcceptedResponse(
    val jobId: String,
)

private fun FormatSnippetResult.toResponseEntity(): ResponseEntity<FormatJobAcceptedResponse> =
    when (this) {
        is FormatSnippetResult.Accepted ->
            ResponseEntity.status(HttpStatus.ACCEPTED).body(FormatJobAcceptedResponse(jobId))
        is FormatSnippetResult.NotFound -> ResponseEntity.notFound().build()
        is FormatSnippetResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

@RestController
@RequestMapping("/format")
class FormatController(
    private val getFormatRules: GetFormatRules,
    private val formatSnippet: FormatSnippet,
) {
    @GetMapping("/rules")
    fun getRules(): ResponseEntity<FormatRulesResponse> {
        val rules = getFormatRules().map { it.toResponse() }
        return ResponseEntity.ok(FormatRulesResponse(rules))
    }

    @PostMapping("/{id}")
    fun format(
        @PathVariable id: String,
        @RequestHeader("X-User-Id") userId: String,
    ): ResponseEntity<FormatJobAcceptedResponse> = formatSnippet(SnippetId(id), UserId(userId)).toResponseEntity()
}
