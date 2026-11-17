package age.of.printscript.demo.format.application

import age.of.printscript.demo.format.application.port.FormatBatchScheduler
import age.of.printscript.demo.format.domain.ActiveFormatRule
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleId
import age.of.printscript.demo.format.domain.FormatRuleRepository
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId

sealed interface UpdateFormatRulesResult {
    data class Success(
        val activeRules: List<ActiveFormatRule>,
    ) : UpdateFormatRulesResult

    data class InvalidRules(
        val unknownRuleIds: List<Int>,
    ) : UpdateFormatRulesResult
}

data class FormatRuleSelection(
    val ruleId: FormatRuleId,
    val value: Int?,
)

fun FormatRuleSelection.toActiveRule(userId: UserId): ActiveFormatRule =
    ActiveFormatRule(
        userId,
        ruleId,
        value,
    )

class UpdateFormatRules(
    val formatRuleRepository: FormatRuleRepository,
    val snippetRepository: SnippetRepository,
    val formatBatchScheduler: FormatBatchScheduler,
) : UseCase {
    operator fun invoke(
        userId: UserId,
        rules: List<FormatRuleSelection>,
    ): UpdateFormatRulesResult {
        val allRulesById = allRules()

        val invalidRules = findInvalid(rules, allRulesById)

        if (invalidRules.isNotEmpty()) {
            return UpdateFormatRulesResult.InvalidRules(invalidRules)
        }

        val savedRules = saveRules(userId, rules)
        val resolvedRulesWithName = resolveRules(savedRules, allRulesById)

        val snippets = snippetRepository.findAllByOwner(userId)

        formatBatchScheduler.scheduleAll(userId, snippets, resolvedRulesWithName)

        return UpdateFormatRulesResult.Success(savedRules)
    }

    private fun resolveRules(
        savedRules: List<ActiveFormatRule>,
        allRulesById: Map<FormatRuleId, FormatRule>,
    ): Map<String, String?> =
        savedRules
            .mapNotNull { active ->
                val rule = allRulesById[active.ruleId] ?: return@mapNotNull null
                rule.name to (active.value ?: rule.defaultValue)?.toString()
            }.toMap()

    private fun saveRules(
        userId: UserId,
        rules: List<FormatRuleSelection>,
    ): List<ActiveFormatRule> =
        formatRuleRepository
            .replaceActiveForUser(userId, rules.map { it.toActiveRule(userId) })

    private fun findInvalid(
        rules: List<FormatRuleSelection>,
        allRulesById: Map<FormatRuleId, FormatRule>,
    ): List<Int> =
        rules
            .filter { it.ruleId !in allRulesById }
            .map { it.ruleId.value }

    private fun allRules(): Map<FormatRuleId, FormatRule> =
        formatRuleRepository
            .findAllRules()
            .associateBy { it.id }
}
