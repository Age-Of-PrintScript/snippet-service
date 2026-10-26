package age.of.printscript.demo.infrastructure.adapter

import age.of.printscript.demo.application.port.JobPublisher
import age.of.printscript.demo.domain.Snippet
import age.of.printscript.jobs.JobRequest
import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.LintJobRequest
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisJobPublisher(
    private val redisTemplate: StringRedisTemplate,
) : JobPublisher {
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
            )
        publish(request)
    }

    private fun publish(job: JobRequest) {
        val payload = JobSerializer.serializeRequest(job)
        redisTemplate.opsForStream<String, String>().add(
            QueueNames.REQUESTS_STREAM,
            mapOf(PAYLOAD_FIELD to payload),
        )
    }
}
