package age.of.printscript.demo.lint.infrastructure.controller

import age.of.printscript.demo.lint.application.LintSnippet
import age.of.printscript.demo.lint.application.LintSnippetResult
import age.of.printscript.demo.lint.application.RuleSelection
import age.of.printscript.demo.lint.application.UpdateLintRules
import age.of.printscript.demo.lint.application.UpdateLintRulesResult
import age.of.printscript.demo.lint.domain.ActiveLintRule
import age.of.printscript.demo.lint.domain.LintRuleId
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class LintJobAcceptedResponse(
    val jobId: String,
)

data class LintRuleSelectionRequest(
    val ruleId: Int,
    val value: String? = null,
)

data class UpdateLintRulesRequest(
    val rules: List<LintRuleSelectionRequest>,
)

data class ActiveLintRuleResponse(
    val ruleId: Int,
    val value: String?,
)

data class ActiveLintRulesResponse(
    val rules: List<ActiveLintRuleResponse>,
)

private fun LintRuleSelectionRequest.toRuleSelection() =
    RuleSelection(
        ruleId = LintRuleId(ruleId),
        value = value,
    )

private fun ActiveLintRule.toResponse() =
    ActiveLintRuleResponse(
        ruleId = ruleId.value,
        value = value,
    )

private fun LintSnippetResult.toResponseEntity(): ResponseEntity<LintJobAcceptedResponse> =
    when (this) {
        is LintSnippetResult.Accepted ->
            ResponseEntity.status(HttpStatus.ACCEPTED).body(LintJobAcceptedResponse(jobId))
        is LintSnippetResult.NotFound -> ResponseEntity.notFound().build()
        is LintSnippetResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

private fun UpdateLintRulesResult.toResponseEntity(): ResponseEntity<Any> =
    when (this) {
        is UpdateLintRulesResult.Success ->
            ResponseEntity.ok(ActiveLintRulesResponse(activeRules.map { it.toResponse() }))
        is UpdateLintRulesResult.InvalidRules ->
            ResponseEntity.badRequest().body(mapOf("unknownRuleIds" to unknownRuleIds))
    }

@RestController
@RequestMapping("/lint")
class LintController(
    private val lintSnippet: LintSnippet,
    private val updateLintRules: UpdateLintRules,
) {
    private val log = LoggerFactory.getLogger(LintController::class.java)

    @PutMapping("/rules")
    fun updateRules(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UpdateLintRulesRequest,
    ): ResponseEntity<Any> =
        when (val result = updateLintRules(UserId(jwt.subject), request.rules.map { it.toRuleSelection() })) {
            is UpdateLintRulesResult.Success -> {
                log.info("Updated {} lint rules", result.activeRules.size)
                result.toResponseEntity()
            }
            is UpdateLintRulesResult.InvalidRules -> {
                log.warn("Failed to update lint rules: unknown rule IDs {}", result.unknownRuleIds)
                result.toResponseEntity()
            }
        }

    @PostMapping("/{id}")
    fun lint(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<LintJobAcceptedResponse> =
        when (val result = lintSnippet(SnippetId(id), UserId(jwt.subject))) {
            is LintSnippetResult.Accepted -> {
                log.info("Lint job accepted for snippet id={}, jobId={}", id, result.jobId)
                result.toResponseEntity()
            }
            is LintSnippetResult.NotFound -> {
                log.warn("Cannot lint snippet id={}: not found", id)
                result.toResponseEntity()
            }
            is LintSnippetResult.Forbidden -> {
                log.warn("Cannot lint snippet id={}: access denied", id)
                result.toResponseEntity()
            }
        }
}
