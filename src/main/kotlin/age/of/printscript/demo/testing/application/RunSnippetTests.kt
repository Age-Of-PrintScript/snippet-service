package age.of.printscript.demo.testing.application

import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.Snippet
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.application.port.TestJobPublisher
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestRepository
import age.of.printscript.demo.testing.domain.SnippetTestSummary
import java.util.UUID

enum class TestSelectionType {
    ALL,
    SELECTED,
}

data class TestSelection(
    val type: TestSelectionType,
    val testIds: List<SnippetTestId> = emptyList(),
)

sealed interface RunSnippetTestsResult {
    data class Accepted(
        val testIds: List<SnippetTestId>,
    ) : RunSnippetTestsResult

    data object NotFound : RunSnippetTestsResult

    data object Forbidden : RunSnippetTestsResult

    data object InvalidSelection : RunSnippetTestsResult
}

class RunSnippetTests(
    private val snippets: SnippetRepository,
    private val tests: SnippetTestRepository,
    private val permissions: PermissionGateway,
    private val jobs: JobRepository,
    private val publisher: TestJobPublisher,
) : UseCase {
    @Suppress("ReturnCount")
    operator fun invoke(
        snippetId: SnippetId,
        userId: UserId,
        selection: TestSelection,
    ): RunSnippetTestsResult {
        val snippet = snippets.findLatest(snippetId) ?: return RunSnippetTestsResult.NotFound
        if (snippet.ownerId != userId && !permissions.hasReadAccess(snippetId, userId)) {
            return RunSnippetTestsResult.Forbidden
        }
        val available = tests.findAllBySnippetId(snippetId).associateBy { it.id }
        val selected = resolveSelection(selection, available) ?: return RunSnippetTestsResult.InvalidSelection
        selected.forEach { enqueue(snippet, userId, it) }
        return RunSnippetTestsResult.Accepted(selected.map { it.id })
    }

    private fun resolveSelection(
        selection: TestSelection,
        available: Map<SnippetTestId, SnippetTestSummary>,
    ): List<SnippetTestSummary>? =
        when (selection.type) {
            TestSelectionType.ALL -> available.values.toList().takeIf { selection.testIds.isEmpty() }
            TestSelectionType.SELECTED ->
                selection.testIds.distinct().mapNotNull { available[it] }.takeIf {
                    it.isNotEmpty() && it.size == selection.testIds.distinct().size
                }
        }

    private fun enqueue(
        snippet: Snippet,
        userId: UserId,
        test: SnippetTestSummary,
    ) {
        val jobId = JobId(UUID.randomUUID().toString())
        jobs.save(
            Job(
                id = jobId,
                snippetId = snippet.id,
                snippetVersion = snippet.version,
                userId = userId,
                type = JobType("TEST"),
                status = JobStatus.PENDING,
            ),
        )
        tests.markPending(test.id, jobId)
        publisher.publishTestJob(jobId.value, snippet, test.contentUrl)
    }
}
