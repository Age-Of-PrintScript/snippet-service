package age.of.printscript.demo.infrastructure.listener

import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.LintJobResponse
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate

class JobResponseStreamListenerTest {
    @Suppress("UNCHECKED_CAST")
    private val streamOperations: StreamOperations<String, String, String> =
        mock(StreamOperations::class.java) as StreamOperations<String, String, String>
    private val redisTemplate: StringRedisTemplate = mock(StringRedisTemplate::class.java)
    private val handler: JobResponseHandler = mock(JobResponseHandler::class.java)
    private val listener = JobResponseStreamListener(redisTemplate, handler)

    @Test
    fun `deserializes response, calls handler, and acknowledges message on success`() {
        `when`(redisTemplate.opsForStream<String, String>()).thenReturn(streamOperations)
        val response = LintJobResponse("job-100", emptyList())
        val payload = JobSerializer.serializeResponse(response)
        val recordId = RecordId.of("1700000000000-0")
        val message = MapRecord.create(QueueNames.RESPONSES_STREAM, mapOf(PAYLOAD_FIELD to payload)).withId(recordId)

        listener.onMessage(message)

        verify(handler).handle(response)
        verify(streamOperations).acknowledge(
            QueueNames.RESPONSES_STREAM,
            QueueNames.SNIPPET_SERVICE_GROUP,
            recordId,
        )
    }

    @Test
    fun `does nothing when message is null`() {
        listener.onMessage(null)

        verifyNoInteractions(handler)
        verifyNoInteractions(redisTemplate)
    }

    @Test
    fun `does nothing when payload field is missing`() {
        val recordId = RecordId.of("1700000000000-1")
        val message = MapRecord.create(QueueNames.RESPONSES_STREAM, emptyMap<String, String>()).withId(recordId)

        listener.onMessage(message)

        verifyNoInteractions(handler)
        verifyNoInteractions(redisTemplate)
    }

    @Test
    fun `acknowledges and does not call handler when payload cannot be deserialized`() {
        `when`(redisTemplate.opsForStream<String, String>()).thenReturn(streamOperations)
        val recordId = RecordId.of("1700000000000-2")
        val message =
            MapRecord
                .create(
                    QueueNames.RESPONSES_STREAM,
                    mapOf(PAYLOAD_FIELD to "invalid-json"),
                ).withId(recordId)

        listener.onMessage(message)

        verifyNoInteractions(handler)
        verify(streamOperations).acknowledge(
            QueueNames.RESPONSES_STREAM,
            QueueNames.SNIPPET_SERVICE_GROUP,
            recordId,
        )
    }

    @Test
    fun `does not acknowledge message when handler throws exception`() {
        val response = LintJobResponse("job-error", emptyList())
        val payload = JobSerializer.serializeResponse(response)
        val recordId = RecordId.of("1700000000000-3")
        val message = MapRecord.create(QueueNames.RESPONSES_STREAM, mapOf(PAYLOAD_FIELD to payload)).withId(recordId)

        doThrow(RuntimeException("Handler failed")).`when`(handler).handle(response)

        listener.onMessage(message)

        verify(handler).handle(response)
        verifyNoInteractions(streamOperations)
    }
}
