package age.of.printscript.demo.snippet.application.port

import age.of.printscript.demo.snippet.domain.SnippetId

interface SnippetStorage {
    fun read(url: String): String

    fun save(
        id: SnippetId,
        version: Int,
        content: String,
    ): String

    fun delete(url: String)
}
