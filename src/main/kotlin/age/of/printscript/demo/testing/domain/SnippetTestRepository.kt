package age.of.printscript.demo.testing.domain

import age.of.printscript.demo.snippet.domain.SnippetId

interface SnippetTestRepository {
    fun findAllBySnippetId(snippetId: SnippetId): List<SnippetTestSummary>

    fun findById(id: SnippetTestId): SnippetTest?

    fun save(test: SnippetTest): SnippetTestSummary
}
