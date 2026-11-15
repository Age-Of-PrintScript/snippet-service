package age.of.printscript.demo.testing.application

import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.snippet.application.FakePermissionGateway
import age.of.printscript.demo.snippet.application.InMemorySnippetRepository
import age.of.printscript.demo.snippet.application.createDummySnippet
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.TestExecution
import age.of.printscript.demo.testing.domain.TestExecutionId
import age.of.printscript.demo.testing.domain.TestExecutionRepository
import age.of.printscript.demo.testing.domain.TestExecutionStatus
import age.of.printscript.demo.testing.domain.TestRun
import age.of.printscript.demo.testing.domain.TestRunId
import age.of.printscript.demo.testing.domain.TestRunRepository
import age.of.printscript.demo.testing.domain.TestRunStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class TestRunUseCasesTest {
    private val snippet = createDummySnippet()
    private val snippetRepository = InMemorySnippetRepository(listOf(snippet))
    private val testRepository = InMemorySnippetTestRepository()
    private val runRepository = MemoryRuns()
    private val executionRepository = MemoryExecutions()
    private val jobRepository = MemoryJobs()
    private val permissions = FakePermissionGateway()
    private val storage = FakeSnippetTestStorage()
    private val alice = UserId("alice")

    private fun addTest(): SnippetTest {
        val test =
            SnippetTest(
                SnippetTestId(UUID.randomUUID().toString()),
                snippet.id,
                alice,
                "https://asset/test-${UUID.randomUUID()}",
            )
        testRepository.save(test)
        return test
    }

    private fun start() =
        StartTestRun(
            snippetRepository,
            testRepository,
            runRepository,
            executionRepository,
            jobRepository,
            permissions,
        )

    @Test
    fun `selected creates one execution and all creates one per test`() {
        val first = addTest()
        addTest()

        val selected = start()(snippet.id, alice, TestSelection(TestSelectionType.SELECTED, listOf(first.id)))
        assertTrue(selected is StartTestRunResult.Accepted)
        assertEquals(1, executionRepository.all.size)
        assertEquals(first.id, executionRepository.all.single().testId)

        val all = start()(snippet.id, alice, TestSelection(TestSelectionType.ALL))
        assertTrue(all is StartTestRunResult.Accepted)
        assertEquals(3, executionRepository.all.size)
        assertEquals(3, jobRepository.all.size)
        assertEquals(2, runRepository.all.size)
    }

    @Test
    fun `rejects an unknown test without creating a run`() {
        addTest()

        val result =
            start()(
                snippet.id,
                alice,
                TestSelection(TestSelectionType.SELECTED, listOf(SnippetTestId(UUID.randomUUID().toString()))),
            )

        assertEquals(StartTestRunResult.InvalidSelection, result)
        assertTrue(runRepository.all.isEmpty())
    }

    @Test
    fun `run polling returns only the requester's results`() {
        val test = addTest()
        val accepted =
            start()(
                snippet.id,
                alice,
                TestSelection(TestSelectionType.SELECTED, listOf(test.id)),
            ) as StartTestRunResult.Accepted
        val get = GetTestRun(runRepository, executionRepository)

        assertEquals(GetTestRunResult.Forbidden, get(accepted.run.id, UserId("bob")))
        val result = get(accepted.run.id, alice) as GetTestRunResult.Success
        assertEquals(TestRunStatus.PENDING, result.run.status)
        assertEquals(TestExecutionStatus.PENDING, result.tests.single().status)
    }

    @Test
    fun `lists test content separately from the snippet`() {
        val id = SnippetTestId(UUID.randomUUID().toString())
        val content = SnippetTestContent(listOf("input"), listOf("expected"))
        val url = storage.save(id, content)
        testRepository.save(SnippetTest(id, snippet.id, alice, url))
        val list =
            ListSnippetTests(
                snippetRepository,
                testRepository,
                storage,
                executionRepository,
                permissions,
            )

        assertEquals(ListSnippetTestsResult.Forbidden, list(snippet.id, UserId("bob")))
        val result = list(snippet.id, alice) as ListSnippetTestsResult.Success
        assertEquals(content, result.tests.single().content)
        assertEquals(null, result.tests.single().lastExecution)
    }
}

private class MemoryRuns : TestRunRepository {
    val all = mutableListOf<TestRun>()

    override fun findById(id: TestRunId): TestRun? = all.find { it.id == id }

    override fun save(testRun: TestRun): TestRun = testRun.also { all.add(it) }
}

private class MemoryExecutions : TestExecutionRepository {
    val all = mutableListOf<TestExecution>()

    override fun findById(id: TestExecutionId): TestExecution? = all.find { it.id == id }

    override fun findAllByRunId(runId: TestRunId): List<TestExecution> = all.filter { it.runId == runId }

    override fun findLatestCompleted(
        testId: SnippetTestId,
        snippetVersion: Int,
    ): TestExecution? = null

    override fun save(testExecution: TestExecution): TestExecution = testExecution.also { all.add(it) }
}

private class MemoryJobs : JobRepository {
    val all = mutableListOf<Job>()

    override fun findById(id: JobId): Job? = all.find { it.id == id }

    override fun save(job: Job): Job = job.also { all.add(it) }
}
