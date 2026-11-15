package age.of.printscript.demo.testing.application

import age.of.printscript.demo.jobs.application.InMemoryJobRepository
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class RunSnippetTestsTest {
    private val snippet = createDummySnippet(version = 3)
    private val snippets = InMemorySnippetRepository(listOf(snippet))
    private val tests = InMemorySnippetTestRepository()
    private val jobs = InMemoryJobRepository()
    private val publisher = FakeTestJobPublisher()
    private val reader = UserId("reader")
    private val permissions = FakePermissionGateway(setOf(snippet.id to reader))
    private val runTests = RunSnippetTests(snippets, tests, permissions, jobs, publisher, CompleteTestJob(jobs, tests))

    @Test
    fun `ALL without ids enqueues one job for each test of the snippet`() {
        val first = addTest()
        val second = addTest()
        val unrelated = addTest(SnippetId("another-snippet"))

        val result = runTests(snippet.id, snippet.ownerId, TestSelection(TestSelectionType.ALL))

        assertEquals(RunSnippetTestsResult.Accepted(listOf(first.id, second.id)), result)
        assertEquals(listOf(first.contentUrl, second.contentUrl), publisher.published.map { it.testUrl })
        val jobIds = publisher.published.map { it.jobId }
        assertEquals(2, jobIds.toSet().size)
        publisher.published.forEach { request ->
            val job = requireNotNull(jobs.findById(JobId(request.jobId)))
            assertEquals(JobStatus.PENDING, job.status)
            assertEquals(JobType("TEST"), job.type)
            assertEquals(snippet.id, job.snippetId)
            assertEquals(snippet.version, job.snippetVersion)
            assertEquals(snippet.ownerId, job.userId)
            assertEquals(snippet, request.snippet)
            val test = tests.savedTests.single { it.first.contentUrl == request.testUrl }.first
            assertEquals(SnippetTestStatus.PENDING, test.status)
            assertEquals(job.id, test.activeJobId)
        }
        assertEquals(unrelated, tests.findById(unrelated.id))
    }

    @Test
    fun `SELECTED runs only the selected test and allows a reader`() {
        val selected = addTest()
        val other = addTest()

        val result = runTests(snippet.id, reader, TestSelection(TestSelectionType.SELECTED, listOf(selected.id)))

        assertEquals(RunSnippetTestsResult.Accepted(listOf(selected.id)), result)
        val request = publisher.published.single()
        assertEquals(selected.contentUrl, request.testUrl)
        assertEquals(reader, jobs.findById(JobId(request.jobId))?.userId)
        assertEquals(other, tests.findById(other.id))
    }

    @Test
    fun `repeated selected ids produce only one job per test`() {
        val test = addTest()

        val result =
            runTests(snippet.id, snippet.ownerId, TestSelection(TestSelectionType.SELECTED, listOf(test.id, test.id)))

        assertEquals(RunSnippetTestsResult.Accepted(listOf(test.id)), result)
        assertEquals(1, publisher.published.size)
    }

    @Test
    fun `invalid selections do not publish or change any tests`() {
        val test = addTest()
        val unrelated = addTest(SnippetId("another-snippet"))
        val selections =
            listOf(
                TestSelection(TestSelectionType.ALL, listOf(test.id)),
                TestSelection(TestSelectionType.SELECTED),
                TestSelection(TestSelectionType.SELECTED, listOf(test.id, unrelated.id)),
                TestSelection(TestSelectionType.SELECTED, listOf(test.id, SnippetTestId("missing"))),
            )

        selections.forEach { selection ->
            assertEquals(RunSnippetTestsResult.InvalidSelection, runTests(snippet.id, snippet.ownerId, selection))
        }

        assertTrue(publisher.published.isEmpty())
        assertEquals(test, tests.findById(test.id))
        assertEquals(unrelated, tests.findById(unrelated.id))
    }

    @Test
    fun `missing or forbidden snippets do not enqueue tests`() {
        val test = addTest()
        val selection = TestSelection(TestSelectionType.ALL)

        assertEquals(RunSnippetTestsResult.NotFound, runTests(SnippetId("missing"), snippet.ownerId, selection))
        assertEquals(RunSnippetTestsResult.Forbidden, runTests(snippet.id, UserId("stranger"), selection))
        assertTrue(publisher.published.isEmpty())
        assertEquals(test, tests.findById(test.id))
    }

    @Test
    fun `ALL on a snippet without tests accepts an empty selection`() {
        assertEquals(
            RunSnippetTestsResult.Accepted(emptyList()),
            runTests(snippet.id, snippet.ownerId, TestSelection(TestSelectionType.ALL)),
        )
        assertTrue(publisher.published.isEmpty())
    }

    @Test
    fun `rerunning a test replaces its active job and marks it pending`() {
        val test = addTest()
        val selection = TestSelection(TestSelectionType.SELECTED, listOf(test.id))
        runTests(snippet.id, snippet.ownerId, selection)
        val previousJob = tests.findById(test.id)?.activeJobId

        runTests(snippet.id, snippet.ownerId, selection)

        val current = requireNotNull(tests.findById(test.id))
        assertNotEquals(previousJob, current.activeJobId)
        assertEquals(JobId(publisher.published.last().jobId), current.activeJobId)
        assertEquals(SnippetTestStatus.PENDING, current.status)
    }

    private fun addTest(snippetId: SnippetId = snippet.id): SnippetTest {
        val id = SnippetTestId(UUID.randomUUID().toString())
        val test =
            SnippetTest(id, snippetId, snippet.ownerId, "https://assets/tests/${id.value}", SnippetTestStatus.PASSED)
        tests.save(test)
        return test
    }

    @Test
    fun `publication failure marks only that test as error and continues with the batch`() {
        val failing = addTest()
        val next = addTest()
        publisher.failingUrls += failing.contentUrl

        val result = runTests(snippet.id, snippet.ownerId, TestSelection(TestSelectionType.ALL))

        assertEquals(RunSnippetTestsResult.Accepted(listOf(failing.id, next.id)), result)
        assertEquals(SnippetTestStatus.ERROR, tests.findById(failing.id)?.status)
        assertEquals(null, tests.findById(failing.id)?.activeJobId)
        assertEquals(SnippetTestStatus.PENDING, tests.findById(next.id)?.status)
        assertEquals(next.contentUrl, publisher.published.single().testUrl)
    }
}
