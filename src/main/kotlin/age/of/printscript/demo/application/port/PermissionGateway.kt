package age.of.printscript.demo.application.port

import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId

interface PermissionGateway {
    fun hasReadAccess(snippetId: SnippetId, userId: UserId): Boolean
}
