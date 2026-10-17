package age.of.printscript.demo.infrastructure.persistance

import age.of.printscript.demo.domain.ActiveLintRule
import age.of.printscript.demo.domain.LintRuleId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.ActiveLintRuleEntity
import age.of.printscript.demo.infrastructure.persistance.entity.LintRuleEntity
import age.of.printscript.demo.infrastructure.persistance.repository.ActiveLintRuleJpaRepository
import age.of.printscript.demo.infrastructure.persistance.repository.LintRuleJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class PostgresLintRuleRepositoryTest {
    private val lintRuleJpaRepository: LintRuleJpaRepository = mock(LintRuleJpaRepository::class.java)
    private val activeLintRuleJpaRepository: ActiveLintRuleJpaRepository =
        mock(ActiveLintRuleJpaRepository::class.java)
    private val repository = PostgresLintRuleRepository(lintRuleJpaRepository, activeLintRuleJpaRepository)

    @Test
    fun `findAllRules returns mapped catalog lint rules`() {
        val entity = LintRuleEntity(id = 1, lintRule = "identifier-format", defaultValue = "camelCase")
        `when`(lintRuleJpaRepository.findAll()).thenReturn(listOf(entity))

        val result = repository.findAllRules()

        assertEquals(1, result.size)
        assertEquals(LintRuleId(1), result.first().id)
        assertEquals("identifier-format", result.first().name)
        assertEquals("camelCase", result.first().defaultValue)
    }

    @Test
    fun `findActiveByUser returns mapped active lint rules for user`() {
        val entity = ActiveLintRuleEntity(userId = "alice", lintRuleId = 1, value = "snake_case")
        `when`(activeLintRuleJpaRepository.findAllByUserId("alice")).thenReturn(listOf(entity))

        val result = repository.findActiveByUser(UserId("alice"))

        assertEquals(1, result.size)
        assertEquals(UserId("alice"), result.first().userId)
        assertEquals(LintRuleId(1), result.first().ruleId)
        assertEquals("snake_case", result.first().value)
    }

    @Test
    fun `replaceActiveForUser deletes previous rules and saves new active rules`() {
        val activeRule = ActiveLintRule(userId = UserId("alice"), ruleId = LintRuleId(2), value = null)
        val savedEntity = ActiveLintRuleEntity(userId = "alice", lintRuleId = 2, value = null)
        doReturn(listOf(savedEntity)).`when`(activeLintRuleJpaRepository).saveAll(anyList())

        val result = repository.replaceActiveForUser(UserId("alice"), listOf(activeRule))

        verify(activeLintRuleJpaRepository).deleteAllByUserId("alice")
        assertEquals(1, result.size)
        assertEquals(LintRuleId(2), result.first().ruleId)
    }
}
