package age.of.printscript.demo.format.application

import age.of.printscript.demo.format.domain.ActiveFormatRule
import age.of.printscript.demo.format.domain.FormatRule
import age.of.printscript.demo.format.domain.FormatRuleId
import age.of.printscript.demo.jobs.application.FakeJobPublisher
import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FormatSnippetTest {
    @Test
    fun `should return ACCEPTED and save PENDING job`() {
        val owner = UserId("alice")
        val snippet = createDummySnippet(ownerId = owner)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val jobPublisher = FakeJobPublisher()
        val jobRepo = InMemoryJobRepository()
        val defaultRules =
            listOf(
                FormatRule(FormatRuleId(1), "lines-after-println", 3),
                FormatRule(FormatRuleId(2), "space-after-colon", null),
                FormatRule(FormatRuleId(2), "space-before-colon", null),
            )
        val activeRules =
            listOf(
                ActiveFormatRule(owner, FormatRuleId(1), 2),
                ActiveFormatRule(owner, FormatRuleId(2), null),
            )
        val formatRuleRepo = InMemoryFormatRuleRepository(defaultRules, activeRules)
        val formatSnippet = FormatSnippet(jobPublisher, formatRuleRepo, snippetRepo, jobRepo)
        val result = formatSnippet(snippet.id, owner)
        assertTrue(result is FormatSnippetResult.Accepted)
        val accepted = result as FormatSnippetResult.Accepted

        val savedJob = jobRepo.findById(JobId(accepted.jobId))
        assertNotNull(savedJob)
        assertEquals(snippet.id, savedJob?.snippetId)
        assertEquals(snippet.version, savedJob?.snippetVersion)
        assertEquals(owner, savedJob?.userId)
        assertEquals(JobType(FORMAT_JOB), savedJob?.type)
        assertEquals(JobStatus.PENDING, savedJob?.status)

        assertEquals(1, jobPublisher.publishedFormatJobs.size)
        val published = jobPublisher.publishedFormatJobs.first()
        assertEquals(accepted.jobId, published.jobId)
        assertEquals(snippet, published.snippet)
        val expected =
            mapOf(
                "lines-after-println" to "2",
                "space-before-colon" to null,
            )
        assertEquals(
            expected,
            published.rules,
        )
    }

    @Test
    fun `should publish empty rules when user has no active rules`() {
        val owner = UserId("alice")
        val snippet = createDummySnippet(ownerId = owner)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val jobPublisher = FakeJobPublisher()
        val jobRepo = InMemoryJobRepository()
        val defaultRules =
            listOf(
                FormatRule(FormatRuleId(1), "lines-after-println", 3),
                FormatRule(FormatRuleId(2), "space-before-colon", null),
            )
        val formatRuleRepo = InMemoryFormatRuleRepository(defaultRules, emptyList())
        val formatSnippet = FormatSnippet(jobPublisher, formatRuleRepo, snippetRepo, jobRepo)

        val result = formatSnippet(snippet.id, owner)

        assertTrue(result is FormatSnippetResult.Accepted)
        assertEquals(1, jobPublisher.publishedFormatJobs.size)
        val published = jobPublisher.publishedFormatJobs.first()
        assertEquals(emptyMap<String, String?>(), published.rules)
    }

    @Test
    fun `should fall back to default value when active rule value is null`() {
        val owner = UserId("alice")
        val snippet = createDummySnippet(ownerId = owner)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val jobPublisher = FakeJobPublisher()
        val jobRepo = InMemoryJobRepository()
        val defaultRules =
            listOf(
                FormatRule(FormatRuleId(1), "lines-after-println", 3),
                FormatRule(FormatRuleId(2), "space-before-colon", null),
            )
        val activeRules =
            listOf(
                ActiveFormatRule(owner, FormatRuleId(1), null),
                ActiveFormatRule(owner, FormatRuleId(2), null),
            )
        val formatRuleRepo = InMemoryFormatRuleRepository(defaultRules, activeRules)
        val formatSnippet = FormatSnippet(jobPublisher, formatRuleRepo, snippetRepo, jobRepo)

        val result = formatSnippet(snippet.id, owner)

        assertTrue(result is FormatSnippetResult.Accepted)
        val published = jobPublisher.publishedFormatJobs.first()
        val expected =
            mapOf(
                "lines-after-println" to "3",
                "space-before-colon" to null,
            )
        assertEquals(expected, published.rules)
    }

    @Test
    fun `should ignore active rules whose rule id does not exist`() {
        val owner = UserId("alice")
        val snippet = createDummySnippet(ownerId = owner)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val jobPublisher = FakeJobPublisher()
        val jobRepo = InMemoryJobRepository()
        val defaultRules =
            listOf(
                FormatRule(FormatRuleId(1), "lines-after-println", 3),
            )
        val activeRules =
            listOf(
                ActiveFormatRule(owner, FormatRuleId(1), 5),
                ActiveFormatRule(owner, FormatRuleId(99), 7),
            )
        val formatRuleRepo = InMemoryFormatRuleRepository(defaultRules, activeRules)
        val formatSnippet = FormatSnippet(jobPublisher, formatRuleRepo, snippetRepo, jobRepo)

        val result = formatSnippet(snippet.id, owner)

        assertTrue(result is FormatSnippetResult.Accepted)
        val published = jobPublisher.publishedFormatJobs.first()
        val expected = mapOf("lines-after-println" to "5")
        assertEquals(expected, published.rules)
    }
}
