package age.of.printscript.demo.application

import age.of.printscript.demo.application.port.SnippetStorage

class FakeSnippetStorage : SnippetStorage {
    private val storage = mutableMapOf<String, String>()

    override fun read(url: String): String = storage[url] ?: ""

    override fun save(
        key: String,
        content: String,
    ): String {
        val url = "https://azure.blob/$key"
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
}
