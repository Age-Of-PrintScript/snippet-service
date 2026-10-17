package age.of.printscript.demo.infrastructure.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "lint_rule")
class LintRuleEntity(
    @Id
    val id: Int,
    @Column(name = "lint_rule", nullable = false, unique = true, length = 100)
    val lintRule: String,
    @Column(name = "default_value", length = 50)
    val defaultValue: String? = null,
)
