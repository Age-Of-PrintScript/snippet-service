package age.of.printscript.demo.application.port

interface SnippetStorage {
    fun read(url: String): String
}
