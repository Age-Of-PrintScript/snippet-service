package age.of.printscript.demo.lint.application

import age.of.printscript.demo.lint.application.port.LintBatchScheduler
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.UserId

data class ScheduledLintBatch(
    val userId: UserId,
    val snippets: List<Snippet>,
    val rules: Map<String, String?>,
)

class FakeLintBatchScheduler : LintBatchScheduler {
    val scheduledBatches = mutableListOf<ScheduledLintBatch>()

    override fun scheduleAll(
        userId: UserId,
        snippets: List<Snippet>,
        rules: Map<String, String?>,
    ) {
        scheduledBatches.add(ScheduledLintBatch(userId, snippets, rules))
    }
}
