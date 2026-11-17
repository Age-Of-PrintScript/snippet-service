package age.of.printscript.demo.snippet.application.port

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId

data class AccessibleSnippet(
    val snippetId: SnippetId,
    val role: String,
)

interface PermissionGateway {
    fun hasReadAccess(
        snippetId: SnippetId,
        userId: UserId,
    ): Boolean

    fun hasWriteAccess(
        snippetId: SnippetId,
        userId: UserId,
    ): Boolean

    /** Registra al usuario autenticado como OWNER del snippet recién creado. */
    fun registerOwner(snippetId: SnippetId)

    /** Lista los snippets (propios y compartidos) a los que el usuario autenticado tiene acceso. */
    fun listAccessibleSnippets(): List<AccessibleSnippet>
}
