package age.of.printscript.demo.format.application.port

import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.UserId

interface FormatBatchScheduler {
    fun scheduleAll(
        userId: UserId,
        snippets: List<Snippet>,
        rules: Map<String, String?>,
    )
}
