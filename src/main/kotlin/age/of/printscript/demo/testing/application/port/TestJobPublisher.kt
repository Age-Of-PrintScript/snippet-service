package age.of.printscript.demo.testing.application.port

import age.of.printscript.demo.snippet.domain.Snippet

interface TestJobPublisher {
    fun publishTestJob(
        jobId: String,
        snippet: Snippet,
        testUrl: String,
    )
}
