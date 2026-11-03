package age.of.printscript.demo.jobs.application

import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository

class InMemoryJobRepository(
    initialJobs: List<Job> = emptyList(),
) : JobRepository {
    private val jobs = initialJobs.associateBy { it.id }.toMutableMap()

    override fun findById(id: JobId): Job? = jobs[id]

    override fun save(job: Job): Job {
        jobs[job.id] = job
        return job
    }
}
