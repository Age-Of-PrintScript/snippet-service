package age.of.printscript.demo.application

import age.of.printscript.demo.application.port.SnippetStorage
import age.of.printscript.demo.domain.SnippetId

class FakeSnippetStorage : SnippetStorage {
    private val storage = mutableMapOf<String, String>()

    override fun read(url: String): String = storage[url] ?: ""

    override fun save(
        id: SnippetId,
        version: Int,
        content: String,
    ): String {
        val url = "https://azure.blob/${id.value}-$version"
        storage[url] = content
        return url
    }

    override fun delete(url: String) {
        storage.remove(url)
    }

    fun set(
        url: String,
        content: String,
    ) {
        storage[url] = content
    }

    fun isEmpty(): Boolean = storage.isEmpty()
}
