package age.of.printscript.demo.format.application

import age.of.printscript.demo.format.domain.ActiveFormatRule
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleRepository
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.domain.UserId

data class FormatRulesSnapshot(
    val rules: List<FormatRule>,
    val activeRules: List<ActiveFormatRule>,
)

class GetFormatRules(
    private val formatRuleRepository: FormatRuleRepository,
) : UseCase {
    operator fun invoke(userId: UserId): FormatRulesSnapshot =
        FormatRulesSnapshot(
            rules = formatRuleRepository.findAllRules(),
            activeRules = formatRuleRepository.findActiveByUser(userId),
        )
}
