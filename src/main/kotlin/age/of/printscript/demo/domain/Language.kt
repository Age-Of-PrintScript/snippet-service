package age.of.printscript.demo.domain


//TODO ESTA horrible que devuelva un string pero no se me ocurre nada ahora
interface Language {
    fun execute(snippet: Snippet): String
    fun lint(snippet: Snippet): String
    fun format(snippet: Snippet): String
    fun validate(snippet: Snippet): String
}
