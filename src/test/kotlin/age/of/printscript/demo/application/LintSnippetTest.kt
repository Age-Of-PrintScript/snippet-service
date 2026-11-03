package age.of.printscript.demo.application

import age.of.printscript.demo.domain.ActiveLintRule
import age.of.printscript.demo.domain.JobId
import age.of.printscript.demo.domain.JobStatus
import age.of.printscript.demo.domain.JobType
import age.of.printscript.demo.domain.LintRule
import age.of.printscript.demo.domain.LintRuleId
import age.of.printscript.demo.domain.LintStatus
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LintSnippetTest {
    @Test
    fun `returns Accepted, saves PENDING job and lint status, and publishes lint job when user is owner`() {
        val owner = UserId("alice")
        val snippet = createDummySnippet(ownerId = owner, version = 2)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val lintRules =
            listOf(
                LintRule(LintRuleId(1), "identifier-format", "camelCase"),
                LintRule(LintRuleId(2), "println-no-expression", null),
                LintRule(LintRuleId(3), "readInput-no-expression", null),
            )
        val activeRules =
            listOf(
                ActiveLintRule(owner, LintRuleId(1), "snake_case"),
                ActiveLintRule(owner, LintRuleId(2), null),
            )
        val lintRuleRepo = InMemoryLintRuleRepository(lintRules, activeRules)
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val jobRepo = InMemoryJobRepository()
        val jobPublisher = FakeJobPublisher()
        val lintSnippet = LintSnippet(jobPublisher, snippetRepo, lintRuleRepo, lintStatusRepo, jobRepo)

        val result = lintSnippet(snippet.id, owner)

        assertTrue(result is LintSnippetResult.Accepted)
        val accepted = result as LintSnippetResult.Accepted

        val savedJob = jobRepo.findById(JobId(accepted.jobId))
        assertNotNull(savedJob)
        assertEquals(snippet.id, savedJob?.snippetId)
        assertEquals(snippet.version, savedJob?.snippetVersion)
        assertEquals(owner, savedJob?.userId)
        assertEquals(JobType(LINT_JOB), savedJob?.type)
        assertEquals(JobStatus.PENDING, savedJob?.status)

        val savedStatus = lintStatusRepo.findByVersion(snippet.id, snippet.version)
        assertNotNull(savedStatus)
        assertEquals(LintStatus.PENDING, savedStatus?.status)

        assertEquals(1, jobPublisher.publishedLintJobs.size)
        val published = jobPublisher.publishedLintJobs.first()
        assertEquals(accepted.jobId, published.jobId)
        assertEquals(snippet, published.snippet)
        assertEquals(
            mapOf(
                "identifier-format" to "snake_case",
                "println-no-expression" to null,
            ),
            published.rules,
        )
    }

    @Test
    fun `falls back to defaultValue when active rule value is null`() {
        val owner = UserId("alice")
        val snippet = createDummySnippet(ownerId = owner)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val lintRules = listOf(LintRule(LintRuleId(1), "identifier-format", "camelCase"))
        val activeRules = listOf(ActiveLintRule(owner, LintRuleId(1), null))
        val lintRuleRepo = InMemoryLintRuleRepository(lintRules, activeRules)
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val jobRepo = InMemoryJobRepository()
        val jobPublisher = FakeJobPublisher()
        val lintSnippet = LintSnippet(jobPublisher, snippetRepo, lintRuleRepo, lintStatusRepo, jobRepo)

        lintSnippet(snippet.id, owner)

        val published = jobPublisher.publishedLintJobs.first()
        assertEquals(mapOf("identifier-format" to "camelCase"), published.rules)
    }

    @Test
    fun `returns NotFound when snippet does not exist`() {
        val snippetRepo = InMemorySnippetRepository()
        val lintRuleRepo = InMemoryLintRuleRepository()
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val jobRepo = InMemoryJobRepository()
        val jobPublisher = FakeJobPublisher()
        val lintSnippet = LintSnippet(jobPublisher, snippetRepo, lintRuleRepo, lintStatusRepo, jobRepo)

        val result = lintSnippet(SnippetId("non-existent"), UserId("alice"))

        assertEquals(LintSnippetResult.NotFound, result)
        assertTrue(jobPublisher.publishedLintJobs.isEmpty())
    }

    @Test
    fun `returns Forbidden when user is not the snippet owner`() {
        val snippet = createDummySnippet(ownerId = UserId("alice"))
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val lintRuleRepo = InMemoryLintRuleRepository()
        val lintStatusRepo = InMemorySnippetLintStatusRepository()
        val jobRepo = InMemoryJobRepository()
        val jobPublisher = FakeJobPublisher()
        val lintSnippet = LintSnippet(jobPublisher, snippetRepo, lintRuleRepo, lintStatusRepo, jobRepo)

        val result = lintSnippet(snippet.id, UserId("bob"))

        assertEquals(LintSnippetResult.Forbidden, result)
        assertTrue(jobPublisher.publishedLintJobs.isEmpty())
    }
}
