package age.of.printscript.demo.infrastructure

import age.of.printscript.demo.domain.Language
import age.of.printscript.demo.domain.Snippet

class PrintScript : Language {
    override fun execute(snippet: Snippet): String {
        TODO("Not yet implemented")
    }

    override fun lint(snippet: Snippet): String {
        TODO("Not yet implemented")
    }

    override fun format(snippet: Snippet): String {
        TODO("Not yet implemented")
    }

    override fun validate(snippet: Snippet): String {
        TODO("Not yet implemented")
    }
}
