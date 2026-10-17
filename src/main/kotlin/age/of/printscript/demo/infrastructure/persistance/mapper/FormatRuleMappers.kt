package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.ActiveFormatRule
import age.of.printscript.demo.domain.FormatRule
import age.of.printscript.demo.domain.FormatRuleId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.ActiveFormatRuleEntity
import age.of.printscript.demo.infrastructure.persistance.entity.FormatRuleEntity

fun FormatRuleEntity.toDomain(): FormatRule =
    FormatRule(
        id = FormatRuleId(id),
        name = formatRule,
        defaultValue = defaultValue,
    )

fun FormatRule.toEntity(): FormatRuleEntity =
    FormatRuleEntity(
        id = id.value,
        formatRule = name,
        defaultValue = defaultValue,
    )

fun ActiveFormatRuleEntity.toDomain(): ActiveFormatRule =
    ActiveFormatRule(
        userId = UserId(userId),
        ruleId = FormatRuleId(formatRuleId),
        value = value,
    )

fun ActiveFormatRule.toEntity(): ActiveFormatRuleEntity =
    ActiveFormatRuleEntity(
        userId = userId.value,
        formatRuleId = ruleId.value,
        value = value,
    )
