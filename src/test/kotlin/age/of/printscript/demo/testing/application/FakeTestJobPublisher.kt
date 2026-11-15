package age.of.printscript.demo.testing.application

import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.testing.application.port.TestJobPublicationException
import age.of.printscript.demo.testing.application.port.TestJobPublisher

class FakeTestJobPublisher : TestJobPublisher {
    data class PublishedTest(
        val jobId: String,
        val snippet: Snippet,
        val testUrl: String,
    )

    val published = mutableListOf<PublishedTest>()
    val failingUrls = mutableSetOf<String>()

    override fun publishTestJob(
        jobId: String,
        snippet: Snippet,
        testUrl: String,
    ) {
        if (testUrl in failingUrls) throw TestJobPublicationException(IllegalStateException("Redis unavailable"))
        published += PublishedTest(jobId, snippet, testUrl)
    }
}
