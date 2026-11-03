package age.of.printscript.demo.format.infrastructure.listener

import age.of.printscript.demo.format.application.CompleteFormatJob
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.infrastructure.listener.JobResponseStrategy
import age.of.printscript.jobs.FormatJobResponse
import age.of.printscript.jobs.JobResponse
import org.springframework.stereotype.Component

@Component
class FormatJobResponseStrategy(
    private val completeFormatJob: CompleteFormatJob,
) : JobResponseStrategy {
    override fun supports(response: JobResponse): Boolean = response is FormatJobResponse

    override fun handle(response: JobResponse) {
        if (response !is FormatJobResponse) return
        completeFormatJob(JobId(response.jobId))
    }
}
