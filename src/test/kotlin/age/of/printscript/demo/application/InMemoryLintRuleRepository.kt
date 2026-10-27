package age.of.printscript.demo.application

import age.of.printscript.demo.domain.ActiveLintRule
import age.of.printscript.demo.domain.LintRule
import age.of.printscript.demo.domain.LintRuleRepository
import age.of.printscript.demo.domain.UserId

class InMemoryLintRuleRepository(
    initialRules: List<LintRule> = emptyList(),
    initialActiveRules: List<ActiveLintRule> = emptyList(),
) : LintRuleRepository {
    private val rules = initialRules.toMutableList()
    private val activeRules = initialActiveRules.toMutableList()

    override fun findAllRules(): List<LintRule> = rules.toList()

    override fun findActiveByUser(userId: UserId): List<ActiveLintRule> = activeRules.filter { it.userId == userId }

    override fun replaceActiveForUser(
        userId: UserId,
        activeRules: List<ActiveLintRule>,
    ): List<ActiveLintRule> {
        this.activeRules.removeIf { it.userId == userId }
        this.activeRules.addAll(activeRules)
        return activeRules
    }
}
