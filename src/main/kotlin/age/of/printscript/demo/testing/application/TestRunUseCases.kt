package age.of.printscript.demo.testing.application

import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.TestExecution
import age.of.printscript.demo.testing.domain.TestExecutionId
import age.of.printscript.demo.testing.domain.TestExecutionRepository
import age.of.printscript.demo.testing.domain.TestExecutionStatus
import age.of.printscript.demo.testing.domain.TestRun
import age.of.printscript.demo.testing.domain.TestRunId
import age.of.printscript.demo.testing.domain.TestRunRepository
import age.of.printscript.demo.testing.domain.TestRunStatus
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

enum class TestSelectionType { SELECTED, ALL }

data class TestSelection(
    val type: TestSelectionType,
    val testIds: List<SnippetTestId> = emptyList(),
)

sealed interface StartTestRunResult {
    data class Accepted(
        val run: TestRun,
    ) : StartTestRunResult

    data object NotFound : StartTestRunResult

    data object Forbidden : StartTestRunResult

    data object InvalidSelection : StartTestRunResult
}

class StartTestRun(
    private val snippets: SnippetRepository,
    private val tests: SnippetTestRepository,
    private val runs: TestRunRepository,
    private val executions: TestExecutionRepository,
    private val jobs: JobRepository,
    private val permissions: PermissionGateway,
) : UseCase {
    @Transactional
    @Suppress("ReturnCount")
    operator fun invoke(
        snippetId: SnippetId,
        userId: UserId,
        selection: TestSelection,
    ): StartTestRunResult {
        val snippet = snippets.findLatest(snippetId) ?: return StartTestRunResult.NotFound
        if (snippet.ownerId != userId && !permissions.hasReadAccess(snippetId, userId)) {
            return StartTestRunResult.Forbidden
        }
        val available = tests.findAllBySnippetId(snippetId).associateBy { it.id }
        val selected =
            when (selection.type) {
                TestSelectionType.ALL -> {
                    if (selection.testIds.isNotEmpty()) return StartTestRunResult.InvalidSelection
                    available.values.toList()
                }
                TestSelectionType.SELECTED -> {
                    if (selection.testIds.isEmpty() ||
                        selection.testIds.size != selection.testIds.distinct().size ||
                        selection.testIds.any { it !in available }
                    ) {
                        return StartTestRunResult.InvalidSelection
                    }
                    selection.testIds.map { available.getValue(it) }
                }
            }
        if (selected.isEmpty()) return StartTestRunResult.InvalidSelection

        val run =
            runs.save(
                TestRun(
                    id = TestRunId(UUID.randomUUID().toString()),
                    snippetId = snippetId,
                    snippetVersion = snippet.version,
                    requestedBy = userId,
                    status = TestRunStatus.PENDING,
                ),
            )
        selected.forEach { test ->
            val jobId = JobId(UUID.randomUUID().toString())
            jobs.save(Job(jobId, snippetId, snippet.version, userId, JobType("TEST"), JobStatus.PENDING))
            executions.save(
                TestExecution(
                    id = TestExecutionId(UUID.randomUUID().toString()),
                    runId = run.id,
                    testId = test.id,
                    jobId = jobId,
                    testUrl = test.contentUrl,
                    status = TestExecutionStatus.PENDING,
                ),
            )
        }
        return StartTestRunResult.Accepted(run)
    }
}

sealed interface GetTestRunResult {
    data class Success(
        val run: TestRun,
        val tests: List<TestExecution>,
    ) : GetTestRunResult

    data object NotFound : GetTestRunResult

    data object Forbidden : GetTestRunResult
}

class GetTestRun(
    private val runs: TestRunRepository,
    private val executions: TestExecutionRepository,
) : UseCase {
    @Suppress("ReturnCount")
    operator fun invoke(
        id: TestRunId,
        userId: UserId,
    ): GetTestRunResult {
        val run = runs.findById(id) ?: return GetTestRunResult.NotFound
        if (run.requestedBy != userId) return GetTestRunResult.Forbidden
        return GetTestRunResult.Success(run, executions.findAllByRunId(id))
    }
}
