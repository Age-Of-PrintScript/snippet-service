package age.of.printscript.demo.lint.application

import age.of.printscript.demo.lint.application.port.LintBatchScheduler
import age.of.printscript.demo.lint.domain.ActiveLintRule
import age.of.printscript.demo.lint.domain.LintRule
import age.of.printscript.demo.lint.domain.LintRuleId
import age.of.printscript.demo.lint.domain.LintRuleRepository
import age.of.printscript.demo.lint.domain.SnippetLintStatusRepository
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId

sealed interface UpdateLintRulesResult {
    data class Success(
        val activeRules: List<ActiveLintRule>,
    ) : UpdateLintRulesResult

    data class InvalidRules(
        val unknownRuleIds: List<Int>,
    ) : UpdateLintRulesResult
}

data class RuleSelection(
    val ruleId: LintRuleId,
    val value: String?,
)

fun RuleSelection.toActiveRule(userId: UserId): ActiveLintRule =
    ActiveLintRule(
        userId,
        ruleId,
        value,
    )

class UpdateLintRules(
    val lintRuleRepository: LintRuleRepository,
    val snippetRepository: SnippetRepository,
    val snippetLintStatusRepository: SnippetLintStatusRepository,
    val lintBatchScheduler: LintBatchScheduler,
) : UseCase {
    operator fun invoke(
        userId: UserId,
        rules: List<RuleSelection>,
    ): UpdateLintRulesResult {
        val allRulesById = allRules()

        val invalidRules = findInvalid(rules, allRulesById)

        if (invalidRules.isNotEmpty()) {
            return UpdateLintRulesResult.InvalidRules(invalidRules)
        }

        val savedRules = saveRules(userId, rules)
        val resolvedRulesWithName = resolveRules(savedRules, allRulesById)

        val snippets = snippetRepository.findAllByOwner(userId)
        snippetLintStatusRepository.markAllPending(snippets)

        lintBatchScheduler.scheduleAll(userId, snippets, resolvedRulesWithName)

        return UpdateLintRulesResult.Success(savedRules)
    }

    private fun resolveRules(
        savedRules: List<ActiveLintRule>,
        allRulesById: Map<LintRuleId, LintRule>,
    ): Map<String, String?> =
        savedRules
            .mapNotNull { active ->
                val rule = allRulesById[active.ruleId] ?: return@mapNotNull null
                rule.name to (active.value ?: rule.defaultValue)
            }.toMap()

    private fun saveRules(
        userId: UserId,
        rules: List<RuleSelection>,
    ): List<ActiveLintRule> =
        lintRuleRepository
            .replaceActiveForUser(userId, rules.map { it.toActiveRule(userId) })

    private fun findInvalid(
        rules: List<RuleSelection>,
        allRulesById: Map<LintRuleId, LintRule>,
    ): List<Int> =
        rules
            .filter { it.ruleId !in allRulesById }
            .map { it.ruleId.value }

    private fun allRules(): Map<LintRuleId, LintRule> =
        lintRuleRepository
            .findAllRules()
            .associateBy { it.id }
}
