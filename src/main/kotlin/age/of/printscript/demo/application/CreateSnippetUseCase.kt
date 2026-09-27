package age.of.printscript.demo.application

import age.of.printscript.demo.domain.Snippet

interface CreateSnippetUseCase {
    fun create(snippet: Snippet): Snippet
}
