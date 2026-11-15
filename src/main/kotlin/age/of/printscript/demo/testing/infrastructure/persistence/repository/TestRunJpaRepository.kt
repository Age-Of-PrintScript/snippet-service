package age.of.printscript.demo.testing.infrastructure.persistence.repository

import age.of.printscript.demo.testing.infrastructure.persistence.entity.TestRunEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TestRunJpaRepository : JpaRepository<TestRunEntity, UUID>
