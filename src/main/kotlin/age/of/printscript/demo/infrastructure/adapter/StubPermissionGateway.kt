package age.of.printscript.demo.infrastructure.adapter

import age.of.printscript.demo.application.port.PermissionGateway
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import org.springframework.stereotype.Component

@Component
class StubPermissionGateway : PermissionGateway {
    override fun hasReadAccess(
        snippetId: SnippetId,
        userId: UserId,
    ): Boolean = true
}
