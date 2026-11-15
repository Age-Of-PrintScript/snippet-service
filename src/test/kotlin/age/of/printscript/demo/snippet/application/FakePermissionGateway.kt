package age.of.printscript.demo.snippet.application

import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId

class FakePermissionGateway(
    private val allowedUsers: Set<Pair<SnippetId, UserId>> = emptySet(),
    private val failOnRegisterOwner: Boolean = false,
) : PermissionGateway {
    val registeredOwnerFor = mutableListOf<SnippetId>()

    override fun hasReadAccess(
        snippetId: SnippetId,
        userId: UserId,
    ): Boolean = (snippetId to userId) in allowedUsers

    override fun registerOwner(snippetId: SnippetId) {
        check(!failOnRegisterOwner) { "permissions-service unavailable" }
        registeredOwnerFor += snippetId
    }
}
