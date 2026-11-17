package age.of.printscript.demo.testing.domain

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.snippet.domain.SnippetId

interface SnippetTestRepository {
    fun findAllBySnippetId(snippetId: SnippetId): List<SnippetTestSummary>

    fun findById(id: SnippetTestId): SnippetTest?

    fun save(test: SnippetTest): SnippetTestSummary

    fun markPending(
        id: SnippetTestId,
        jobId: JobId,
    )

    fun completeIfActive(
        jobId: JobId,
        status: SnippetTestStatus,
    )

    fun invalidateForSnippet(snippetId: SnippetId)

    fun deleteById(id: SnippetTestId)

    fun deleteAllBySnippetId(snippetId: SnippetId)
}
