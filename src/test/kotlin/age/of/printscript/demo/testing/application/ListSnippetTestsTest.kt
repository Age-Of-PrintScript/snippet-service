package age.of.printscript.demo.testing.application

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class ListSnippetTestsTest {
    private val snippet = createDummySnippet()
    private val snippets = InMemorySnippetRepository(listOf(snippet))
    private val tests = InMemorySnippetTestRepository()
    private val storage = FakeSnippetTestStorage()
    private val listTests = ListSnippetTests(snippets, tests, storage, FakePermissionGateway())

    @Test
    fun `new tests are listed as not run`() {
        val test = addTest()

        val result = listTests(snippet.id, snippet.ownerId) as ListSnippetTestsResult.Success

        assertEquals(SnippetTestStatus.NOT_RUN, result.tests.single().status)
        assertEquals(storage.read(test.contentUrl), result.tests.single().content)
        assertNull(tests.findById(test.id)?.activeJobId)
    }

    @Test
    fun `listing returns the status stored on each test`() {
        val id = SnippetTestId(UUID.randomUUID().toString())
        val contentUrl = storage.save(id, SnippetTestContent(listOf("input"), listOf("expected")))
        tests.save(
            SnippetTest(
                id = id,
                snippetId = snippet.id,
                ownerId = snippet.ownerId,
                contentUrl = contentUrl,
                status = SnippetTestStatus.PASSED,
                activeJobId = JobId(UUID.randomUUID().toString()),
            ),
        )

        val result = listTests(snippet.id, snippet.ownerId) as ListSnippetTestsResult.Success

        assertEquals(SnippetTestStatus.PASSED, result.tests.single().status)
    }

    @Test
    fun `listing requires access to the snippet`() {
        addTest()

        assertEquals(ListSnippetTestsResult.Forbidden, listTests(snippet.id, UserId("bob")))
    }

    private fun addTest(): SnippetTest {
        val id = SnippetTestId(UUID.randomUUID().toString())
        val url = storage.save(id, SnippetTestContent(listOf("input"), listOf("expected")))
        val test = SnippetTest(id, snippet.id, snippet.ownerId, url)
        tests.save(test)
        return test
    }
}
