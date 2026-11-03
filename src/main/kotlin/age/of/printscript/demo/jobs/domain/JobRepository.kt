package age.of.printscript.demo.jobs.domain

interface JobRepository {
    fun findById(id: JobId): Job?

    fun save(job: Job): Job
}
