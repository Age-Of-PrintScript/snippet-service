package age.of.printscript.demo.lint.domain

import age.of.printscript.demo.snippet.domain.UserId

@JvmInline
value class LintRuleId(
    val value: Int,
)

data class LintRule(
    val id: LintRuleId,
    val name: String,
    val defaultValue: String?,
)

data class ActiveLintRule(
    val userId: UserId,
    val ruleId: LintRuleId,
    val value: String?,
)
