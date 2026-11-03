package age.of.printscript.demo.snippet.application.port

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId

interface PermissionGateway {
    fun hasReadAccess(
        snippetId: SnippetId,
        userId: UserId,
    ): Boolean
}
