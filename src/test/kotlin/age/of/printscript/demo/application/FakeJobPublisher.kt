package age.of.printscript.demo.application

import age.of.printscript.demo.application.port.JobPublisher
import age.of.printscript.demo.domain.Snippet

data class PublishedLintJob(
    val jobId: String,
    val snippet: Snippet,
    val rules: Map<String, String?>,
)

data class PublishedFormatJob(
    val jobId: String,
    val snippet: Snippet,
    val rules: Map<String, String?>,
)

class FakeJobPublisher : JobPublisher {
    val publishedLintJobs = mutableListOf<PublishedLintJob>()
    val publishedFormatJobs = mutableListOf<PublishedFormatJob>()

    override fun publishLintJob(
        jobId: String,
        snippet: Snippet,
        rules: Map<String, String?>,
    ) {
        publishedLintJobs.add(PublishedLintJob(jobId, snippet, rules))
    }

    override fun publishFormatJob(
        jobId: String,
        snippet: Snippet,
        rules: Map<String, String?>,
    ) {
        publishedFormatJobs.add(PublishedFormatJob(jobId, snippet, rules))
    }
}
