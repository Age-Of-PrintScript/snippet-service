package age.of.printscript.demo.application

import age.of.printscript.demo.domain.ActiveFormatRule
import age.of.printscript.demo.domain.FormatRule
import age.of.printscript.demo.domain.FormatRuleId
import age.of.printscript.demo.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FormatSnippetTest {
    @Test
    fun `should return ACCEPTED`() {
        val owner = UserId("alice")
        val snippet = createDummySnippet(ownerId = owner)
        val snippetRepo = InMemorySnippetRepository(listOf(snippet))
        val jobPublisher = FakeJobPublisher()
        val defaultRules =
            listOf(
                FormatRule(
                    FormatRuleId(
                        1,
                    ),
                    "lines-after-println",
                    3,
                ),
                FormatRule(
                    FormatRuleId(
                        2,
                    ),
                    "space-after-colon",
                    null,
                ),
                FormatRule(
                    FormatRuleId(
                        2,
                    ),
                    "space-before-colon",
                    null,
                ),
            )
        val activeRules =
            listOf(
                ActiveFormatRule(owner, FormatRuleId(1), 2),
                ActiveFormatRule(owner, FormatRuleId(2), null),
            )
        val formatRuleRepo = InMemoryFormatRuleRepository(defaultRules, activeRules)
        val formatSnippet = FormatSnippet(jobPublisher, formatRuleRepo, snippetRepo)
        val result = formatSnippet(snippet.id, owner)
        assertTrue(result is FormatSnippetResult.Accepted)
        val accepted = result as FormatSnippetResult.Accepted

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
        val defaultRules =
            listOf(
                FormatRule(FormatRuleId(1), "lines-after-println", 3),
                FormatRule(FormatRuleId(2), "space-before-colon", null),
            )
        val formatRuleRepo = InMemoryFormatRuleRepository(defaultRules, emptyList())
        val formatSnippet = FormatSnippet(jobPublisher, formatRuleRepo, snippetRepo)

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
        val formatSnippet = FormatSnippet(jobPublisher, formatRuleRepo, snippetRepo)

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
        val formatSnippet = FormatSnippet(jobPublisher, formatRuleRepo, snippetRepo)

        val result = formatSnippet(snippet.id, owner)

        assertTrue(result is FormatSnippetResult.Accepted)
        val published = jobPublisher.publishedFormatJobs.first()
        val expected = mapOf("lines-after-println" to "5")
        assertEquals(expected, published.rules)
    }
}
