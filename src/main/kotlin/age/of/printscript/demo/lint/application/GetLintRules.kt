package age.of.printscript.demo.lint.application

import age.of.printscript.demo.lint.domain.ActiveLintRule
import age.of.printscript.demo.lint.domain.LintRule
import age.of.printscript.demo.lint.domain.LintRuleRepository
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.domain.UserId

data class LintRulesSnapshot(
    val rules: List<LintRule>,
    val activeRules: List<ActiveLintRule>,
)

class GetLintRules(
    private val lintRuleRepository: LintRuleRepository,
) : UseCase {
    operator fun invoke(userId: UserId): LintRulesSnapshot =
        LintRulesSnapshot(
            rules = lintRuleRepository.findAllRules(),
            activeRules = lintRuleRepository.findActiveByUser(userId),
        )
}
