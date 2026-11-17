package age.of.printscript.demo.testing.application

import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class ListSnippetTestStatusesTest {
    private val snippet = createDummySnippet()
    private val snippets = InMemorySnippetRepository(listOf(snippet))
    private val tests = InMemorySnippetTestRepository()
    private val reader = UserId("reader")
    private val permissions = FakePermissionGateway(setOf(snippet.id to reader))
    private val listStatuses = ListSnippetTestStatuses(snippets, tests, permissions)

    @Test
    fun `returns current statuses only for the requested snippet`() {
        val expected =
            SnippetTestStatus.entries.map { status ->
                val id = addTest(snippet.id, status)
                TestStatusItem(id, status)
            }
        addTest(SnippetId(UUID.randomUUID().toString()), SnippetTestStatus.PASSED)

        val result = listStatuses(snippet.id, snippet.ownerId) as ListSnippetTestStatusesResult.Success

        assertEquals(expected, result.tests)
    }

    @Test
    fun `users with read access can poll statuses`() {
        val id = addTest(snippet.id, SnippetTestStatus.PENDING)

        val result = listStatuses(snippet.id, reader) as ListSnippetTestStatusesResult.Success

        assertEquals(listOf(TestStatusItem(id, SnippetTestStatus.PENDING)), result.tests)
    }

    @Test
    fun `returns an empty list when the snippet has no tests`() {
        assertEquals(ListSnippetTestStatusesResult.Success(emptyList()), listStatuses(snippet.id, snippet.ownerId))
    }

    @Test
    fun `returns forbidden when the user has no access`() {
        assertEquals(ListSnippetTestStatusesResult.Forbidden, listStatuses(snippet.id, UserId("stranger")))
    }

    @Test
    fun `returns not found when the snippet does not exist`() {
        assertEquals(
            ListSnippetTestStatusesResult.NotFound,
            listStatuses(SnippetId(UUID.randomUUID().toString()), snippet.ownerId),
        )
    }

    private fun addTest(
        snippetId: SnippetId,
        status: SnippetTestStatus,
    ): SnippetTestId {
        val id = SnippetTestId(UUID.randomUUID().toString())
        tests.save(
            SnippetTest(id, snippetId, snippet.ownerId, "Status test", "https://assets/tests/${id.value}", status),
        )
        return id
    }
}
