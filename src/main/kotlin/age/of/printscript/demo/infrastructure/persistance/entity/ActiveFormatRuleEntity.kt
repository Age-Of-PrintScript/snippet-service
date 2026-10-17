package age.of.printscript.demo.infrastructure.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable

data class ActiveFormatRuleEntityId(
    val userId: String = "",
    val formatRuleId: Int = 0,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

@Entity
@Table(name = "active_format_rule")
@IdClass(ActiveFormatRuleEntityId::class)
class ActiveFormatRuleEntity(
    @Id
    @Column(name = "user_id", nullable = false, length = 128)
    val userId: String,
    @Id
    @Column(name = "format_rule_id", nullable = false)
    val formatRuleId: Int,
    @Column(name = "value")
    val value: Int? = null,
)
