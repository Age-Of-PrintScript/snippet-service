package age.of.printscript.demo.lint.application

import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.lint.application.port.LintJobPublisher
import age.of.printscript.demo.lint.domain.LintRuleRepository
import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatusRepository
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import java.util.UUID

const val LINT_JOB = "LINT"

class LintSnippet(
    private val jobPublisher: LintJobPublisher,
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
