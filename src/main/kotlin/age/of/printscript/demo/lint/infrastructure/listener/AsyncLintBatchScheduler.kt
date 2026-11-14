package age.of.printscript.demo.lint.infrastructure.listener

import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.lint.application.LINT_JOB
import age.of.printscript.demo.lint.application.port.LintBatchScheduler
import age.of.printscript.demo.lint.application.port.LintJobPublisher
import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatusRepository
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.UserId
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AsyncLintBatchScheduler(
    private val jobPublisher: LintJobPublisher,
    private val jobRepository: JobRepository,
    private val snippetLintStatusRepository: SnippetLintStatusRepository,
) : LintBatchScheduler {
    private val logger = LoggerFactory.getLogger(AsyncLintBatchScheduler::class.java)

    @Async
    override fun scheduleAll(
        userId: UserId,
        snippets: List<Snippet>,
        rules: Map<String, String?>,
    ) {
        snippets.forEach { snippet ->
            scheduleOne(userId, snippet, rules)
        }
    }

    private fun scheduleOne(
        userId: UserId,
        snippet: Snippet,
        rules: Map<String, String?>,
    ) {
        val jobId = UUID.randomUUID().toString()
        runCatching {
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
            jobPublisher.publishLintJob(jobId, snippet, rules)
        }.onFailure { error ->
            logger.error("Failed to schedule lint job for snippet ${snippet.id.value}", error)
            snippetLintStatusRepository.save(
                SnippetLintStatus(
                    snippetId = snippet.id,
                    version = snippet.version,
                    status = LintStatus.FAILED,
                ),
            )
        }
    }
}
