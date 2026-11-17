package age.of.printscript.demo.jobs.infrastructure.adapter

import age.of.printscript.demo.format.application.port.FormatJobPublisher
import age.of.printscript.demo.jobs.infrastructure.RedisTraceHeaders
import age.of.printscript.demo.lint.application.port.LintJobPublisher
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.testing.application.port.TestJobPublicationException
import age.of.printscript.demo.testing.application.port.TestJobPublisher
import age.of.printscript.jobs.FormatJobRequest
import age.of.printscript.jobs.JobRequest
import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.LintJobRequest
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
import age.of.printscript.jobs.TestJobRequest
import org.springframework.dao.DataAccessException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisJobPublisher(
    private val redisTemplate: StringRedisTemplate,
) : LintJobPublisher,
    FormatJobPublisher,
    TestJobPublisher {
    override fun publishLintJob(
        jobId: String,
        snippet: Snippet,
        rules: Map<String, String?>,
    ) {
        val request =
            LintJobRequest(
                jobId = jobId,
                snippetId = snippet.id.value,
                snippetUrl = snippet.contentUrl,
                languageVersion = snippet.languageVersion,
                rules = rules,
                traceHeaders = RedisTraceHeaders.capture(),
            )
        publish(request)
    }

    override fun publishFormatJob(
        jobId: String,
        snippet: Snippet,
        rules: Map<String, String?>,
    ) {
        val request =
            FormatJobRequest(
                jobId = jobId,
                snippetId = snippet.id.value,
                snippetUrl = snippet.contentUrl,
                languageVersion = snippet.languageVersion,
                rules = rules,
                traceHeaders = RedisTraceHeaders.capture(),
            )
        publish(request)
    }

    override fun publishTestJob(
        jobId: String,
        snippet: Snippet,
        testUrl: String,
    ) {
        val request =
            TestJobRequest(
                jobId = jobId,
                snippetId = snippet.id.value,
                snippetUrl = snippet.contentUrl,
                languageVersion = snippet.languageVersion,
                testUrl = testUrl,
                traceHeaders = RedisTraceHeaders.capture(),
            )
        try {
            publish(request)
        } catch (failure: DataAccessException) {
            throw TestJobPublicationException(failure)
        }
    }

    private fun publish(job: JobRequest) {
        val serialized = JobSerializer.serializeRequest(job)
        redisTemplate.opsForStream<String, String>().add(
            QueueNames.REQUESTS_STREAM,
            mapOf(PAYLOAD_FIELD to serialized),
        )
    }
}
