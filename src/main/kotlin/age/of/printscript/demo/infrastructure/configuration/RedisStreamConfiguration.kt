package age.of.printscript.demo.infrastructure.configuration

import age.of.printscript.demo.infrastructure.listener.JobResponseStreamListener
import age.of.printscript.jobs.QueueNames
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions
import org.springframework.data.redis.stream.Subscription
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.Duration
import java.util.UUID
import java.util.concurrent.Executors

private typealias JobStreamContainer = StreamMessageListenerContainer<String, MapRecord<String, String, String>>

@Configuration
class RedisStreamConfiguration {
    private val logger = LoggerFactory.getLogger(RedisStreamConfiguration::class.java)

    @Bean
    fun jobResponseStreamContainer(connectionFactory: RedisConnectionFactory): JobStreamContainer {
        val options =
            StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(POLL_TIMEOUT_SECONDS))
                .executor(Executors.newFixedThreadPool(THREAD_POOL_SIZE))
                .build()

        return StreamMessageListenerContainer.create(connectionFactory, options)
    }

    @Bean
    fun jobResponseSubscription(
        container: StreamMessageListenerContainer<String, MapRecord<String, String, String>>,
        stringRedisTemplate: StringRedisTemplate,
        jobResponseStreamListener: JobResponseStreamListener,
    ): Subscription {
        initConsumerGroup(stringRedisTemplate)

        val consumerName = resolveConsumerName()

        val subscription =
            container.receive(
                Consumer.from(QueueNames.SNIPPET_SERVICE_GROUP, consumerName),
                StreamOffset.create(QueueNames.RESPONSES_STREAM, ReadOffset.lastConsumed()),
                jobResponseStreamListener,
            )

        container.start()
        logger.info(
            "Subscribed consumer '{}' in group '{}' to stream '{}'",
            consumerName,
            QueueNames.SNIPPET_SERVICE_GROUP,
            QueueNames.RESPONSES_STREAM,
        )
        return subscription
    }

    private fun initConsumerGroup(redisTemplate: StringRedisTemplate) {
        runCatching {
            redisTemplate.opsForStream<String, String>().createGroup(
                QueueNames.RESPONSES_STREAM,
                ReadOffset.latest(),
                QueueNames.SNIPPET_SERVICE_GROUP,
            )
        }.onSuccess {
            logger.info(
                "Created consumer group '{}' on stream '{}'",
                QueueNames.SNIPPET_SERVICE_GROUP,
                QueueNames.RESPONSES_STREAM,
            )
        }.onFailure { exception ->
            logger.info(
                "Consumer group '{}' on stream '{}' already initialized or ready",
                QueueNames.SNIPPET_SERVICE_GROUP,
                QueueNames.RESPONSES_STREAM,
                exception,
            )
        }
    }

    private fun resolveConsumerName(): String =
        try {
            val suffix = UUID.randomUUID().toString().take(CONSUMER_NAME_SUFFIX_LENGTH)
            "${InetAddress.getLocalHost().hostName}-$suffix"
        } catch (exception: UnknownHostException) {
            logger.debug("Could not resolve local host name, using fallback identifier", exception)
            val fallbackSuffix = UUID.randomUUID().toString().take(CONSUMER_NAME_SUFFIX_LENGTH)
            "worker-$fallbackSuffix"
        }

    private companion object {
        const val POLL_TIMEOUT_SECONDS = 2L
        const val THREAD_POOL_SIZE = 4
        const val CONSUMER_NAME_SUFFIX_LENGTH = 6
    }
}
