package age.of.printscript.demo.format.application

import age.of.printscript.demo.format.application.port.FormatJobPublisher
import age.of.printscript.demo.format.domain.FormatRuleRepository
import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import java.util.UUID

const val FORMAT_JOB = "FORMAT"

class FormatSnippet(
    private val jobPublisher: FormatJobPublisher,
    private val formatRuleRepository: FormatRuleRepository,
    private val snippetRepository: SnippetRepository,
    private val jobRepository: JobRepository,
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
            jobRepository.save(
                Job(
                    id = JobId(jobId),
                    snippetId = snippet.id,
                    snippetVersion = snippet.version,
                    userId = userId,
                    type = JobType(FORMAT_JOB),
                    status = JobStatus.PENDING,
                ),
            )
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
