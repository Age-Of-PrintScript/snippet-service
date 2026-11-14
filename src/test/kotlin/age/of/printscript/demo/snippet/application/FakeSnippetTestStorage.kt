package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.application.port.SnippetTestStorage
import age.of.printscript.demo.snippet.domain.SnippetTest
import age.of.printscript.demo.snippet.domain.SnippetTestContent

class FakeSnippetTestStorage : SnippetTestStorage {
    private val storage = mutableMapOf<String, SnippetTest>()
    val deletedUrls = mutableListOf<String>()

    override fun read(url: String): SnippetTestContent {
        val test = storage[url] ?: error("Test asset not found")
        return SnippetTestContent(test.inputs, test.outputs)
    }

    override fun save(test: SnippetTest): String {
        val url = "https://asset/v1/asset/snippet-tests/${test.id.value}"
        storage[url] = test
        return url
    }

    override fun delete(url: String) {
        storage.remove(url)
        deletedUrls += url
    }

    fun isEmpty(): Boolean = storage.isEmpty()
}
