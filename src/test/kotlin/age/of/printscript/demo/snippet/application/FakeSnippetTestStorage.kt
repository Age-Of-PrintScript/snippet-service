package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.application.port.SnippetTestStorage
import age.of.printscript.demo.snippet.domain.SnippetTest

class FakeSnippetTestStorage : SnippetTestStorage {
    private val storage = mutableMapOf<String, SnippetTest>()
    val deletedUrls = mutableListOf<String>()

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
