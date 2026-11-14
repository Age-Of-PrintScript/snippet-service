package age.of.printscript.demo.snippet.application.port

import age.of.printscript.demo.snippet.domain.SnippetTest
import age.of.printscript.demo.snippet.domain.SnippetTestSummary

interface SnippetTestRepository {
    fun save(
        test: SnippetTest,
        contentUrl: String,
    ): SnippetTestSummary
}
