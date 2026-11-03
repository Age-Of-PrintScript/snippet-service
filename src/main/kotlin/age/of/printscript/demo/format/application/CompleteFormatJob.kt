package age.of.printscript.demo.format.application

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.shared.application.UseCase

class CompleteFormatJob(
    private val jobRepository: JobRepository,
) : UseCase {
    operator fun invoke(jobId: JobId) {
        val job = jobRepository.findById(jobId) ?: return
        jobRepository.save(job.copy(status = JobStatus.DONE))
    }
}
