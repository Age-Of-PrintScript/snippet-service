package age.of.printscript.demo.snippet.infrastructure.adapter

import age.of.printscript.demo.snippet.application.port.AccessibleSnippet
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class PermissionService(
    private val permissionRestClient: RestClient,
) : PermissionGateway {
    private val readPermission = "snippet:read"
    private val writePermission = "snippet:write"

    override fun hasReadAccess(
        snippetId: SnippetId,
        userId: UserId,
    ): Boolean = hasPermission(snippetId, userId, readPermission)

    override fun hasWriteAccess(
        snippetId: SnippetId,
        userId: UserId,
    ): Boolean = hasPermission(snippetId, userId, writePermission)

    private fun hasPermission(
        snippetId: SnippetId,
        userId: UserId,
        permission: String,
    ): Boolean {
        val permissions =
            permissionRestClient
                .get()
                .uri("/permissions?userId={userId}&snippetId={snippetId}", userId.value, snippetId.value)
                .retrieve()
                .body<List<PermissionResponse>>()

        return permissions?.any { it.name == permission } ?: false
    }

    override fun registerOwner(snippetId: SnippetId) {
        permissionRestClient
            .post()
            .uri("/snippets/{snippetId}/owner", snippetId.value)
            .retrieve()
            .toBodilessEntity()
    }

    override fun listAccessibleSnippets(): List<AccessibleSnippet> {
        val items =
            permissionRestClient
                .get()
                .uri("/snippets")
                .retrieve()
                .body<List<AccessibleSnippetResponse>>()
                .orEmpty()

        return items.map { AccessibleSnippet(SnippetId(it.snippetId), it.role) }
    }

    internal data class PermissionResponse(
        val id: String = "",
        val name: String = "",
    )

    internal data class AccessibleSnippetResponse(
        val snippetId: String = "",
        val role: String = "",
    )
}
