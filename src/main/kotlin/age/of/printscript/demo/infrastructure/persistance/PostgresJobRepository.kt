package age.of.printscript.demo.infrastructure.persistance

import age.of.printscript.demo.domain.Job
import age.of.printscript.demo.domain.JobId
import age.of.printscript.demo.domain.JobRepository
import age.of.printscript.demo.infrastructure.persistance.mapper.toDomain
import age.of.printscript.demo.infrastructure.persistance.mapper.toEntity
import age.of.printscript.demo.infrastructure.persistance.repository.JobJpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PostgresJobRepository(
    private val jpaRepository: JobJpaRepository,
) : JobRepository {
    override fun findById(id: JobId): Job? {
        val uuid = UUID.fromString(id.value)
        return jpaRepository
            .findById(uuid)
            .orElse(null)
            ?.toDomain()
    }

    override fun save(job: Job): Job {
        val entity = job.toEntity()
        return jpaRepository.save(entity).toDomain()
    }
}
