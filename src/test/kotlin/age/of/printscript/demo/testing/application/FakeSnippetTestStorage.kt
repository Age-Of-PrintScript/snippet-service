package age.of.printscript.demo.testing.application

import age.of.printscript.demo.testing.application.port.SnippetTestStorage
import age.of.printscript.demo.testing.domain.SnippetTestContent
import age.of.printscript.demo.testing.domain.SnippetTestId

class FakeSnippetTestStorage : SnippetTestStorage {
    private val storage = mutableMapOf<String, SnippetTestContent>()
    val deletedUrls = mutableListOf<String>()

    override fun read(url: String): SnippetTestContent = storage[url] ?: error("Test asset not found")

    override fun save(
        id: SnippetTestId,
        content: SnippetTestContent,
    ): String {
        val url = "https://asset/v1/asset/snippet-tests/${id.value}"
        storage[url] = content
        return url
    }

    override fun delete(url: String) {
        storage.remove(url)
        deletedUrls += url
    }

    fun isEmpty(): Boolean = storage.isEmpty()
}
