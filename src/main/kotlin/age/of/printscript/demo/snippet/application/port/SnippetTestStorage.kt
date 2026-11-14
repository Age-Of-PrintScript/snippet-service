package age.of.printscript.demo.snippet.application.port

import age.of.printscript.demo.snippet.domain.SnippetTest

interface SnippetTestStorage {
    fun save(test: SnippetTest): String

    fun delete(url: String)
}
