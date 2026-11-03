package age.of.printscript.demo.application.port

import age.of.printscript.demo.domain.Snippet

interface JobPublisher {
    fun publishLintJob(
        jobId: String,
        snippet: Snippet,
        rules: Map<String, String?>,
    )

    fun publishFormatJob(
        jobId: String,
        snippet: Snippet,
        rules: Map<String, String?>,
    )
}
