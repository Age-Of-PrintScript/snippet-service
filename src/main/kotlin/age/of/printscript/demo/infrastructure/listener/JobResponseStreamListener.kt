package age.of.printscript.demo.infrastructure.listener

import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamListener
import org.springframework.stereotype.Component

@Component
class JobResponseStreamListener(
    private val redisTemplate: StringRedisTemplate,
    private val jobResponseHandler: JobResponseHandler,
) : StreamListener<String, MapRecord<String, String, String>> {
    private val logger = LoggerFactory.getLogger(JobResponseStreamListener::class.java)

    override fun onMessage(message: MapRecord<String, String, String>?) {
        val payload = message?.value?.get(PAYLOAD_FIELD) ?: return
        val response =
            runCatching {
                JobSerializer.deserializeResponse(payload)
            }.getOrElse { error ->
                logger.error("Failed to deserialize JobResponse from message {}", message.id, error)
                acknowledge(message)
                return
            }

        runCatching {
            jobResponseHandler.handle(response)
        }.onSuccess {
            acknowledge(message)
        }.onFailure { error ->
            logger.error("Failed to handle JobResponse for jobId={}", response.jobId, error)
        }
    }

    private fun acknowledge(message: MapRecord<String, String, String>) {
        redisTemplate.opsForStream<String, String>().acknowledge(
            QueueNames.RESPONSES_STREAM,
            QueueNames.SNIPPET_SERVICE_GROUP,
            message.id,
        )
    }
}
