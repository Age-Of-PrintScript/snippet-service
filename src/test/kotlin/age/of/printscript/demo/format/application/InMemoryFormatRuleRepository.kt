package age.of.printscript.demo.format.application

import age.of.printscript.demo.format.domain.ActiveFormatRule
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleRepository
import age.of.printscript.demo.snippet.domain.UserId

class InMemoryFormatRuleRepository(
    initialRules: List<FormatRule> = emptyList(),
    initialActiveRules: List<ActiveFormatRule> = emptyList(),
) : FormatRuleRepository {
    private val rules = initialRules.toMutableList()
    private val activeRules = initialActiveRules.toMutableList()

    override fun findAllRules(): List<FormatRule> = rules.toList()

    override fun findActiveByUser(userId: UserId): List<ActiveFormatRule> = activeRules.filter { it.userId == userId }

    override fun replaceActiveForUser(
        userId: UserId,
        activeRules: List<ActiveFormatRule>,
    ): List<ActiveFormatRule> {
        this.activeRules.removeIf { it.userId == userId }
        this.activeRules.addAll(activeRules)
        return activeRules
    }
}
