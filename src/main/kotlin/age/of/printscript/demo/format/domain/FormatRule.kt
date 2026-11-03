package age.of.printscript.demo.format.domain

import age.of.printscript.demo.snippet.domain.UserId

@JvmInline
value class FormatRuleId(
    val value: Int,
)

data class FormatRule(
    val id: FormatRuleId,
    val name: String,
    val defaultValue: Int?,
)

data class ActiveFormatRule(
    val userId: UserId,
    val ruleId: FormatRuleId,
    val value: Int?,
)
