package age.of.printscript.demo.domain

interface LintRuleRepository {
    fun findAllRules(): List<LintRule>

    fun findActiveByUser(userId: UserId): List<ActiveLintRule>

    fun replaceActiveForUser(
        userId: UserId,
        activeRules: List<ActiveLintRule>,
    ): List<ActiveLintRule>
}
