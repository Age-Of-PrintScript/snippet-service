package age.of.printscript.demo.jobs.infrastructure.persistence.repository

import age.of.printscript.demo.jobs.infrastructure.persistence.entity.JobEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JobJpaRepository : JpaRepository<JobEntity, UUID>
