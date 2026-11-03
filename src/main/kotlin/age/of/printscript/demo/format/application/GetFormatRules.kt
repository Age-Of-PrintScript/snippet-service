package age.of.printscript.demo.format.application

import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleRepository
import age.of.printscript.demo.shared.application.UseCase

class GetFormatRules(
    private val formatRuleRepository: FormatRuleRepository,
) : UseCase {
    operator fun invoke(): List<FormatRule> = formatRuleRepository.findAllRules()
}
