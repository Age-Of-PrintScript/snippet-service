package age.of.printscript.demo.domain

interface SnippetLintStatusRepository {
    fun findByVersion(
        snippetId: SnippetId,
        version: Int,
    ): SnippetLintStatus?

    fun save(lintStatus: SnippetLintStatus): SnippetLintStatus
}
