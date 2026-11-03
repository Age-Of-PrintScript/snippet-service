package age.of.printscript.demo.application

import age.of.printscript.demo.application.port.JobPublisher
import age.of.printscript.demo.domain.Job
import age.of.printscript.demo.domain.JobId
import age.of.printscript.demo.domain.JobRepository
import age.of.printscript.demo.domain.JobStatus
import age.of.printscript.demo.domain.JobType
import age.of.printscript.demo.domain.LintRuleRepository
import age.of.printscript.demo.domain.LintStatus
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetLintStatus
import age.of.printscript.demo.domain.SnippetLintStatusRepository
import age.of.printscript.demo.domain.SnippetRepository
import age.of.printscript.demo.domain.UserId
import java.util.UUID

const val LINT_JOB = "LINT"

class LintSnippet(
    private val jobPublisher: JobPublisher,
    private val snippetRepository: SnippetRepository,
    private val lintRuleRepository: LintRuleRepository,
    private val snippetLintStatusRepository: SnippetLintStatusRepository,
    private val jobRepository: JobRepository,
) : UseCase {
    operator fun invoke(
        id: SnippetId,
        userId: UserId,
    ): LintSnippetResult {
        val snippet = snippetRepository.findLatest(id) ?: return LintSnippetResult.NotFound
        return if (snippet.ownerId != userId) {
            LintSnippetResult.Forbidden
        } else {
            val rules = resolveActiveRules(userId)
            val jobId = UUID.randomUUID().toString()
            jobRepository.save(
                Job(
                    id = JobId(jobId),
                    snippetId = snippet.id,
                    snippetVersion = snippet.version,
                    userId = userId,
                    type = JobType(LINT_JOB),
                    status = JobStatus.PENDING,
                ),
            )
            snippetLintStatusRepository.save(
                SnippetLintStatus(
                    snippetId = snippet.id,
                    version = snippet.version,
                    status = LintStatus.PENDING,
                ),
            )
            jobPublisher.publishLintJob(jobId, snippet, rules)
            LintSnippetResult.Accepted(jobId)
        }
    }

    private fun resolveActiveRules(userId: UserId): Map<String, String?> {
        val rulesById = lintRuleRepository.findAllRules().associateBy { it.id }
        return lintRuleRepository
            .findActiveByUser(userId)
            .mapNotNull { active ->
                val rule = rulesById[active.ruleId] ?: return@mapNotNull null
                rule.name to (active.value ?: rule.defaultValue)
            }.toMap()
    }
}

sealed interface LintSnippetResult {
    data object NotFound : LintSnippetResult

    data object Forbidden : LintSnippetResult

    data class Accepted(
        val jobId: String,
    ) : LintSnippetResult
}
