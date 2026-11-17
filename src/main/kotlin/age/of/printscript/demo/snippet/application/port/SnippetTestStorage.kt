package age.of.printscript.demo.snippet.application.port

import age.of.printscript.demo.snippet.domain.SnippetTest
import age.of.printscript.demo.snippet.domain.SnippetTestContent

interface SnippetTestStorage {
    fun read(url: String): SnippetTestContent

    fun save(test: SnippetTest): String

    fun delete(url: String)
}
