package age.of.printscript.demo.lint.domain

import age.of.printscript.demo.snippet.domain.UserId

interface LintRuleRepository {
    fun findAllRules(): List<LintRule>

    fun findActiveByUser(userId: UserId): List<ActiveLintRule>

    fun replaceActiveForUser(
        userId: UserId,
        activeRules: List<ActiveLintRule>,
    ): List<ActiveLintRule>
}
