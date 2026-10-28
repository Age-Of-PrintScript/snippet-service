package age.of.printscript.demo.infrastructure.persistance.repository

import age.of.printscript.demo.infrastructure.persistance.entity.JobEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JobJpaRepository : JpaRepository<JobEntity, UUID>
