package age.of.printscript.demo.domain

interface FormatRuleRepository {
    fun findAllRules(): List<FormatRule>

    fun findActiveByUser(userId: UserId): List<ActiveFormatRule>

    fun replaceActiveForUser(
        userId: UserId,
        activeRules: List<ActiveFormatRule>,
    ): List<ActiveFormatRule>
}
