package age.of.printscript.demo.format.application.port

import age.of.printscript.demo.snippet.domain.Snippet

interface FormatJobPublisher {
    fun publishFormatJob(
        jobId: String,
        snippet: Snippet,
        rules: Map<String, String?>,
    )
}
