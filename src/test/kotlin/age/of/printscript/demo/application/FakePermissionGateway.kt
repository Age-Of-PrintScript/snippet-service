package age.of.printscript.demo.application

import age.of.printscript.demo.application.port.PermissionGateway
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId

class FakePermissionGateway(
    private val allowedUsers: Set<Pair<SnippetId, UserId>> = emptySet(),
) : PermissionGateway {
    override fun hasReadAccess(
        snippetId: SnippetId,
        userId: UserId,
    ): Boolean = (snippetId to userId) in allowedUsers
}
