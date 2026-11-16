package age.of.printscript.demo.testing.infrastructure.listener

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.infrastructure.listener.JobResponseStrategy
import age.of.printscript.demo.testing.application.CompleteTestJob
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import age.of.printscript.jobs.JobResponse
import age.of.printscript.jobs.SystemErrorJobResponse
import age.of.printscript.jobs.TestJobResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TestJobResponseStrategy(
    private val completeTestJob: CompleteTestJob,
) : JobResponseStrategy {
    override fun supports(response: JobResponse) = response is TestJobResponse || response is SystemErrorJobResponse

    @Transactional
    override fun handle(response: JobResponse) {
        when (response) {
            is TestJobResponse ->
                completeTestJob(
                    JobId(response.jobId),
                    if (response.success) SnippetTestStatus.PASSED else SnippetTestStatus.FAILED,
                    response.message,
                )
            is SystemErrorJobResponse ->
                completeTestJob(JobId(response.jobId), SnippetTestStatus.ERROR, response.reason)
            else -> Unit
        }
    }
}
