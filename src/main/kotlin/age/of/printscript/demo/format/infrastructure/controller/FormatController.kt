package age.of.printscript.demo.format.infrastructure.controller

import age.of.printscript.demo.format.application.FormatRuleSelection
import age.of.printscript.demo.format.application.FormatSnippet
import age.of.printscript.demo.format.application.FormatSnippetResult
import age.of.printscript.demo.format.application.GetFormatRules
import age.of.printscript.demo.format.application.UpdateFormatRules
import age.of.printscript.demo.format.application.UpdateFormatRulesResult
import age.of.printscript.demo.format.domain.ActiveFormatRule
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleId
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
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

data class FormatRuleSelectionRequest(
    val ruleId: Int,
    val value: Int? = null,
)

data class UpdateFormatRulesRequest(
    val rules: List<FormatRuleSelectionRequest>,
)

data class ActiveFormatRuleResponse(
    val ruleId: Int,
    val value: Int?,
)

data class ActiveFormatRulesResponse(
    val rules: List<ActiveFormatRuleResponse>,
)

private fun FormatRuleSelectionRequest.toRuleSelection() =
    FormatRuleSelection(
        ruleId = FormatRuleId(ruleId),
        value = value,
    )

private fun ActiveFormatRule.toResponse() =
    ActiveFormatRuleResponse(
        ruleId = ruleId.value,
        value = value,
    )

private fun FormatSnippetResult.toResponseEntity(): ResponseEntity<FormatJobAcceptedResponse> =
    when (this) {
        is FormatSnippetResult.Accepted ->
            ResponseEntity.status(HttpStatus.ACCEPTED).body(FormatJobAcceptedResponse(jobId))
        is FormatSnippetResult.NotFound -> ResponseEntity.notFound().build()
        is FormatSnippetResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

private fun UpdateFormatRulesResult.toResponseEntity(): ResponseEntity<Any> =
    when (this) {
        is UpdateFormatRulesResult.Success ->
            ResponseEntity.ok(ActiveFormatRulesResponse(activeRules.map { it.toResponse() }))
        is UpdateFormatRulesResult.InvalidRules ->
            ResponseEntity.badRequest().body(mapOf("unknownRuleIds" to unknownRuleIds))
    }

@RestController
@RequestMapping("/format")
class FormatController(
    private val getFormatRules: GetFormatRules,
    private val formatSnippet: FormatSnippet,
    private val updateFormatRules: UpdateFormatRules,
) {
    private val log = LoggerFactory.getLogger(FormatController::class.java)

    @GetMapping("/rules")
    fun getRules(): ResponseEntity<FormatRulesResponse> {
        val rules = getFormatRules().map { it.toResponse() }
        log.info("Fetched {} format rules", rules.size)
        return ResponseEntity.ok(FormatRulesResponse(rules))
    }

    @PutMapping("/rules")
    fun updateRules(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UpdateFormatRulesRequest,
    ): ResponseEntity<Any> =
        when (val result = updateFormatRules(UserId(jwt.subject), request.rules.map { it.toRuleSelection() })) {
            is UpdateFormatRulesResult.Success -> {
                log.info("Updated {} format rules", result.activeRules.size)
                result.toResponseEntity()
            }
            is UpdateFormatRulesResult.InvalidRules -> {
                log.warn("Failed to update format rules: unknown rule IDs {}", result.unknownRuleIds)
                result.toResponseEntity()
            }
        }

    @PostMapping("/{id}")
    fun format(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<FormatJobAcceptedResponse> =
        when (val result = formatSnippet(SnippetId(id), UserId(jwt.subject))) {
            is FormatSnippetResult.Accepted -> {
                log.info("Format job accepted for snippet id={}, jobId={}", id, result.jobId)
                result.toResponseEntity()
            }
            is FormatSnippetResult.NotFound -> {
                log.warn("Cannot format snippet id={}: not found", id)
                result.toResponseEntity()
            }
            is FormatSnippetResult.Forbidden -> {
                log.warn("Cannot format snippet id={}: access denied", id)
                result.toResponseEntity()
            }
        }
}
