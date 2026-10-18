package age.of.printscript.demo.infrastructure.adapter

import age.of.printscript.demo.application.port.PermissionGateway
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class PermissionService(
    private val permissionRestClient: RestClient,
) : PermissionGateway {
    private val readPermission = "snippet:read"

    override fun hasReadAccess(
        snippetId: SnippetId,
        userId: UserId,
    ): Boolean {
        val permissions =
            permissionRestClient
                .get()
                .uri("/permissions?userId={userId}&snippetId={snippetId}", userId.value, snippetId.value)
                .retrieve()
                .body<List<PermissionResponse>>()

        return permissions?.any { it.name == readPermission } ?: false
    }

    internal data class PermissionResponse(
        val id: String = "",
        val name: String = "",
    )
}
