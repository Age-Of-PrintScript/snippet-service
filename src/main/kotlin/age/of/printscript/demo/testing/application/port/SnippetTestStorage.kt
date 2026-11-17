package age.of.printscript.demo.testing.application.port

import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId

interface SnippetTestStorage {
    fun read(url: String): SnippetTestContent

    fun save(
        id: SnippetTestId,
        content: SnippetTestContent,
    ): String

    fun delete(url: String)
}
