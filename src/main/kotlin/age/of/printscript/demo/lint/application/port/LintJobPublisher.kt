package age.of.printscript.demo.lint.application.port

import age.of.printscript.demo.snippet.domain.Snippet

interface LintJobPublisher {
    fun publishLintJob(
        jobId: String,
        snippet: Snippet,
        rules: Map<String, String?>,
    )
}
