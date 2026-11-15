package age.of.printscript.demo.testing.domain

interface SnippetTestRepository {
    fun save(test: SnippetTest): SnippetTestSummary
}
