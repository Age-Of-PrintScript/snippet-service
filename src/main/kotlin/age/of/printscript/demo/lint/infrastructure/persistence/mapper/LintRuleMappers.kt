package age.of.printscript.demo.lint.infrastructure.persistence.mapper

import age.of.printscript.demo.lint.domain.ActiveLintRule
import age.of.printscript.demo.lint.domain.LintRule
import age.of.printscript.demo.lint.domain.LintRuleId
import age.of.printscript.demo.lint.infrastructure.persistence.entity.ActiveLintRuleEntity
import age.of.printscript.demo.lint.infrastructure.persistence.entity.LintRuleEntity
import age.of.printscript.demo.snippet.domain.UserId

fun LintRuleEntity.toDomain(): LintRule =
    LintRule(
        id = LintRuleId(id),
        name = lintRule,
        defaultValue = defaultValue,
    )

fun LintRule.toEntity(): LintRuleEntity =
    LintRuleEntity(
        id = id.value,
        lintRule = name,
        defaultValue = defaultValue,
    )

fun ActiveLintRuleEntity.toDomain(): ActiveLintRule =
    ActiveLintRule(
        userId = UserId(userId),
        ruleId = LintRuleId(lintRuleId),
        value = value,
    )

fun ActiveLintRule.toEntity(): ActiveLintRuleEntity =
    ActiveLintRuleEntity(
        userId = userId.value,
        lintRuleId = ruleId.value,
        value = value,
    )
