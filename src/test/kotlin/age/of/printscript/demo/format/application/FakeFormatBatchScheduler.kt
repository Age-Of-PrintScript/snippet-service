package age.of.printscript.demo.format.application

import age.of.printscript.demo.format.application.port.FormatBatchScheduler
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.UserId

data class ScheduledFormatBatch(
    val userId: UserId,
    val snippets: List<Snippet>,
    val rules: Map<String, String?>,
)

class FakeFormatBatchScheduler : FormatBatchScheduler {
    val scheduledBatches = mutableListOf<ScheduledFormatBatch>()

    override fun scheduleAll(
        userId: UserId,
        snippets: List<Snippet>,
        rules: Map<String, String?>,
    ) {
        scheduledBatches.add(ScheduledFormatBatch(userId, snippets, rules))
    }
}
