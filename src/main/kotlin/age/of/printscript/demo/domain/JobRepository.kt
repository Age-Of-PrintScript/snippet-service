package age.of.printscript.demo.domain

interface JobRepository {
    fun findById(id: JobId): Job?

    fun save(job: Job): Job
}
