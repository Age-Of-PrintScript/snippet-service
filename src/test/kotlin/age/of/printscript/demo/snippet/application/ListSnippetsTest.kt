package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.lint.application.InMemorySnippetLintStatusRepository
import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatus
import age.of.printscript.demo.snippet.application.port.AccessibleSnippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ListSnippetsTest {
    @Test
    fun `returns owned and shared snippets with compliance and role`() {
        val own = createDummySnippet(id = SnippetId("s1"), ownerId = UserId("alice")).copy(title = "Mi snippet")
        val shared = createDummySnippet(id = SnippetId("s2"), ownerId = UserId("bob")).copy(title = "Compartido")
        val other = createDummySnippet(id = SnippetId("s3"), ownerId = UserId("bob")).copy(title = "Ajeno")

        val accessible =
            listOf(
                AccessibleSnippet(SnippetId("s1"), "OWNER"),
                AccessibleSnippet(SnippetId("s2"), "EDITOR"),
            )
        val permissions = FakePermissionGateway(accessibleSnippets = accessible)
        val repo = InMemorySnippetRepository(listOf(own, shared, other))
        val lintRepo =
            InMemorySnippetLintStatusRepository(
                listOf(SnippetLintStatus(SnippetId("s1"), version = 1, status = LintStatus.COMPLIANT)),
            )

        val result = ListSnippets(permissions, repo, lintRepo)(page = 0, pageSize = 10)

        assertEquals(2L, result.total)
        assertEquals(2, result.snippets.size)

        val first = result.snippets.first { it.snippetId == SnippetId("s1") }
        assertEquals("Mi snippet", first.title)
        assertEquals(LintStatus.COMPLIANT, first.compliance)
        assertEquals(UserId("alice"), first.author)
        assertEquals("OWNER", first.role)

        val second = result.snippets.first { it.snippetId == SnippetId("s2") }
        assertEquals(LintStatus.PENDING, second.compliance)
        assertEquals(UserId("bob"), second.author)
        assertEquals("EDITOR", second.role)
    }

    @Test
    fun `returns empty page when user has no accessible snippets`() {
        val repo = InMemorySnippetRepository(listOf(createDummySnippet(id = SnippetId("s1"))))
        val result = ListSnippets(FakePermissionGateway(), repo, InMemorySnippetLintStatusRepository())()

        assertEquals(0L, result.total)
        assertTrue(result.snippets.isEmpty())
    }

    @Test
    fun `filters by title and paginates`() {
        val s1 = createDummySnippet(id = SnippetId("s1")).copy(title = "Alpha uno")
        val s2 = createDummySnippet(id = SnippetId("s2")).copy(title = "Alpha dos")
        val s3 = createDummySnippet(id = SnippetId("s3")).copy(title = "Beta")
        val permissions =
            FakePermissionGateway(
                accessibleSnippets = listOf("s1", "s2", "s3").map { AccessibleSnippet(SnippetId(it), "OWNER") },
            )
        val repo = InMemorySnippetRepository(listOf(s1, s2, s3))
        val useCase = ListSnippets(permissions, repo, InMemorySnippetLintStatusRepository())

        val page0 = useCase(page = 0, pageSize = 1, name = "alpha")
        val page1 = useCase(page = 1, pageSize = 1, name = "alpha")

        assertEquals(2L, page0.total)
        assertEquals(1, page0.snippets.size)
        assertEquals(1, page1.snippets.size)
    }

    @Test
    fun `filters by relation, language, compliance and sorts results`() {
        val s1 =
            createDummySnippet(
                id = SnippetId("s1"),
                ownerId = UserId("alice"),
            ).copy(title = "Zebra", language = "printscript")
        val s2 =
            createDummySnippet(
                id = SnippetId("s2"),
                ownerId = UserId("alice"),
            ).copy(title = "Alpha", language = "python")
        val s3 =
            createDummySnippet(
                id = SnippetId("s3"),
                ownerId = UserId("bob"),
            ).copy(title = "Beta", language = "printscript")
        val accessible =
            listOf(
                AccessibleSnippet(SnippetId("s1"), "OWNER"),
                AccessibleSnippet(SnippetId("s2"), "OWNER"),
                AccessibleSnippet(SnippetId("s3"), "VIEWER"),
            )
        val permissions = FakePermissionGateway(accessibleSnippets = accessible)
        val repo = InMemorySnippetRepository(listOf(s1, s2, s3))
        val lintRepo =
            InMemorySnippetLintStatusRepository(
                listOf(
                    SnippetLintStatus(SnippetId("s1"), version = 1, status = LintStatus.COMPLIANT),
                    SnippetLintStatus(SnippetId("s2"), version = 1, status = LintStatus.NON_COMPLIANT),
                    SnippetLintStatus(SnippetId("s3"), version = 1, status = LintStatus.COMPLIANT),
                ),
            )
        val useCase = ListSnippets(permissions, repo, lintRepo)

        val ownedOnly =
            useCase(relation = SnippetRelation.OWNED, sortBy = SnippetSortField.NAME, sortDir = SortDirection.ASC)
        assertEquals(listOf("Alpha", "Zebra"), ownedOnly.snippets.map { it.title })

        val sharedOnly = useCase(relation = SnippetRelation.SHARED)
        assertEquals(listOf("Beta"), sharedOnly.snippets.map { it.title })

        val compliantPrintscript =
            useCase(
                language = "printscript",
                compliance = LintStatus.COMPLIANT,
                sortBy = SnippetSortField.NAME,
                sortDir = SortDirection.DESC,
            )
        assertEquals(listOf("Zebra", "Beta"), compliantPrintscript.snippets.map { it.title })
    }
}
