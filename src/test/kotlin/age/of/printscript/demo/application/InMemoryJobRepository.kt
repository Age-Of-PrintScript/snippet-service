package age.of.printscript.demo.application

import age.of.printscript.demo.domain.Job
import age.of.printscript.demo.domain.JobId
import age.of.printscript.demo.domain.JobRepository

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
