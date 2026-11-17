package age.of.printscript.demo.format.infrastructure.persistence.repository

import age.of.printscript.demo.format.infrastructure.persistence.entity.ActiveFormatRuleEntity
import age.of.printscript.demo.format.infrastructure.persistence.entity.ActiveFormatRuleEntityId
import age.of.printscript.demo.format.infrastructure.persistence.entity.FormatRuleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface FormatRuleJpaRepository : JpaRepository<FormatRuleEntity, Int>

interface ActiveFormatRuleJpaRepository : JpaRepository<ActiveFormatRuleEntity, ActiveFormatRuleEntityId> {
    fun findAllByUserId(userId: String): List<ActiveFormatRuleEntity>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ActiveFormatRuleEntity a WHERE a.userId = :userId")
    fun deleteAllByUserId(userId: String)
}
