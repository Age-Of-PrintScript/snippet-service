package age.of.printscript.demo.domain

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
