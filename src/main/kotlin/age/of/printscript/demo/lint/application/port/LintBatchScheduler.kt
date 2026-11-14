package age.of.printscript.demo.lint.application.port

import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.UserId

interface LintBatchScheduler {
    fun scheduleAll(
        userId: UserId,
        snippets: List<Snippet>,
        rules: Map<String, String?>,
    )
}
