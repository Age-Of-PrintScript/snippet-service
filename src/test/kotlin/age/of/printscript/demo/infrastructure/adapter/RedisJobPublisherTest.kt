package age.of.printscript.demo.infrastructure.adapter

import age.of.printscript.demo.application.createDummySnippet
import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.LintJobRequest
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate

class RedisJobPublisherTest {
    @Suppress("UNCHECKED_CAST")
    private val streamOperations: StreamOperations<String, String, String> =
        mock(StreamOperations::class.java) as StreamOperations<String, String, String>
    private val redisTemplate: StringRedisTemplate = mock(StringRedisTemplate::class.java)
    private val publisher = RedisJobPublisher(redisTemplate)

    @Test
    fun `publishLintJob serializes LintJobRequest and adds record to requests stream`() {
        `when`(redisTemplate.opsForStream<String, String>()).thenReturn(streamOperations)
        val snippet = createDummySnippet()
        val rules = mapOf("identifier-format" to "camelCase", "println-no-expression" to null)

        @Suppress("UNCHECKED_CAST")
        val mapCaptor: ArgumentCaptor<Map<String, String>> =
            ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, String>>
        `when`(streamOperations.add(eq(QueueNames.REQUESTS_STREAM), mapCaptor.capture()))
            .thenReturn(RecordId.of("1700000000-0"))

        publisher.publishLintJob("job-123", snippet, rules)

        verify(streamOperations).add(eq(QueueNames.REQUESTS_STREAM), mapCaptor.capture())
        val payload = mapCaptor.value[PAYLOAD_FIELD]!!
        val deserialized = JobSerializer.deserializeRequest(payload)
        assertEquals(
            LintJobRequest(
                jobId = "job-123",
                snippetId = snippet.id.value,
                snippetUrl = snippet.contentUrl,
                languageVersion = snippet.languageVersion,
                rules = rules,
            ),
            deserialized,
        )
    }
}
