package age.of.printscript.demo.testing.application

import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.testing.application.port.TestJobPublisher

class FakeTestJobPublisher : TestJobPublisher {
    data class PublishedTest(
        val jobId: String,
        val snippet: Snippet,
        val testUrl: String,
    )

    val published = mutableListOf<PublishedTest>()

    override fun publishTestJob(
        jobId: String,
        snippet: Snippet,
        testUrl: String,
    ) {
        published += PublishedTest(jobId, snippet, testUrl)
    }
}
