package age.of.printscript.demo.testing.application

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestStatus

class CompleteTestJob(
    private val jobs: JobRepository,
    private val tests: SnippetTestRepository,
) : UseCase {
    operator fun invoke(
        jobId: JobId,
        status: SnippetTestStatus,
        message: String? = null,
    ) {
        require(status in setOf(SnippetTestStatus.PASSED, SnippetTestStatus.FAILED, SnippetTestStatus.ERROR))
        val job = jobs.findById(jobId) ?: return
        if (job.type.value != "TEST") return
        tests.completeIfActive(jobId, status)
        if (job.status == JobStatus.PENDING) {
            jobs.save(
                job.copy(
                    status = if (status == SnippetTestStatus.ERROR) JobStatus.FAILED else JobStatus.DONE,
                    result = message,
                ),
            )
        }
    }
}
