package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.ActiveLintRule
import age.of.printscript.demo.domain.LintRule
import age.of.printscript.demo.domain.LintRuleId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.ActiveLintRuleEntity
import age.of.printscript.demo.infrastructure.persistance.entity.LintRuleEntity

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
