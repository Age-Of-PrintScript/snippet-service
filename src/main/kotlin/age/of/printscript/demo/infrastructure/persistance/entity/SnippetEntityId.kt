package age.of.printscript.demo.infrastructure.persistance.entity

import java.io.Serializable
import java.util.UUID

data class SnippetEntityId(
    val id: UUID = UUID.randomUUID(),
    val version: Int = 1,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
