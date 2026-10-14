package age.of.printscript.demo.application.port

interface SnippetStorage {
    fun read(url: String): String

    fun save(
        key: String,
        content: String,
    ): String

    fun delete(url: String)
}
