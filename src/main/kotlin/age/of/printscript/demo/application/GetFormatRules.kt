package age.of.printscript.demo.application

import age.of.printscript.demo.domain.FormatRule
import age.of.printscript.demo.domain.FormatRuleRepository

class GetFormatRules(
    private val formatRuleRepository: FormatRuleRepository,
) : UseCase {
    operator fun invoke(): List<FormatRule> = formatRuleRepository.findAllRules()
}
