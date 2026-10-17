package age.of.printscript.demo.infrastructure.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable

data class ActiveLintRuleEntityId(
    val userId: String = "",
    val lintRuleId: Int = 0,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

@Entity
@Table(name = "active_lint_rule")
@IdClass(ActiveLintRuleEntityId::class)
class ActiveLintRuleEntity(
    @Id
    @Column(name = "user_id", nullable = false, length = 128)
    val userId: String,
    @Id
    @Column(name = "lint_rule_id", nullable = false)
    val lintRuleId: Int,
    @Column(name = "value", length = 50)
    val value: String? = null,
)
