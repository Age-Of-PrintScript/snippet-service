package age.of.printscript.demo.format.infrastructure.listener

import age.of.printscript.demo.format.application.FORMAT_JOB
import age.of.printscript.demo.format.application.port.FormatBatchScheduler
import age.of.printscript.demo.format.application.port.FormatJobPublisher
import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.UserId
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AsyncFormatBatchScheduler(
    private val jobPublisher: FormatJobPublisher,
    private val jobRepository: JobRepository,
) : FormatBatchScheduler {
    private val logger = LoggerFactory.getLogger(AsyncFormatBatchScheduler::class.java)

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
        val job =
            Job(
                id = JobId(jobId),
                snippetId = snippet.id,
                snippetVersion = snippet.version,
                userId = userId,
                type = JobType(FORMAT_JOB),
                status = JobStatus.PENDING,
            )
        runCatching {
            jobRepository.save(job)
            jobPublisher.publishFormatJob(jobId, snippet, rules)
        }.onFailure { error ->
            logger.error("Failed to schedule format job for snippet ${snippet.id.value}", error)
            jobRepository.save(job.copy(status = JobStatus.FAILED))
        }
    }
}
