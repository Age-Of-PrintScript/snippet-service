package age.of.printscript.demo.format.infrastructure.persistence.mapper

import age.of.printscript.demo.format.domain.ActiveFormatRule
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleId
import age.of.printscript.demo.format.infrastructure.persistence.entity.ActiveFormatRuleEntity
import age.of.printscript.demo.format.infrastructure.persistence.entity.FormatRuleEntity
import age.of.printscript.demo.snippet.domain.UserId

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
