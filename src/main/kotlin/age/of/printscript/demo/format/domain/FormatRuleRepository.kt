package age.of.printscript.demo.format.domain

import age.of.printscript.demo.snippet.domain.UserId

interface FormatRuleRepository {
    fun findAllRules(): List<FormatRule>

    fun findActiveByUser(userId: UserId): List<ActiveFormatRule>

    fun replaceActiveForUser(
        userId: UserId,
        activeRules: List<ActiveFormatRule>,
    ): List<ActiveFormatRule>
}
