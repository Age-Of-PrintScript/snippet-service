package age.of.printscript.demo.application

import age.of.printscript.demo.application.port.JobPublisher
import age.of.printscript.demo.domain.FormatRuleRepository
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetRepository
import age.of.printscript.demo.domain.UserId
import java.util.UUID

class FormatSnippet(
    private val jobPublisher: JobPublisher,
    private val formatRuleRepository: FormatRuleRepository,
    private val snippetRepository: SnippetRepository,
) : UseCase {
    operator fun invoke(
        snippetId: SnippetId,
        userId: UserId,
    ): FormatSnippetResult {
        val snippet = snippetRepository.findLatest(snippetId) ?: return FormatSnippetResult.NotFound
        return if (snippet.ownerId != userId) {
            FormatSnippetResult.Forbidden
        } else {
            val activeRules = resolveActiveRules(userId)
            val jobId = UUID.randomUUID().toString()
            jobPublisher.publishFormatJob(jobId, snippet, activeRules)
            FormatSnippetResult.Accepted(jobId)
        }
    }

    // código casi duplicado con LintSnippet, podríamos extraer el método
    private fun resolveActiveRules(userId: UserId): Map<String, String?> {
        val rulesById = formatRuleRepository.findAllRules().associateBy { it.id }
        return formatRuleRepository
            .findActiveByUser(userId)
            .mapNotNull { active ->
                val rule = rulesById[active.ruleId] ?: return@mapNotNull null
                rule.name to (active.value ?: rule.defaultValue)?.toString()
            }.toMap()
    }
}

sealed interface FormatSnippetResult {
    object NotFound : FormatSnippetResult

    object Forbidden : FormatSnippetResult

    data class Accepted(
        val jobId: String,
    ) : FormatSnippetResult
}
