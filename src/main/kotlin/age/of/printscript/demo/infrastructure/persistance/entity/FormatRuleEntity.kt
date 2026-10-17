package age.of.printscript.demo.infrastructure.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "format_rule")
class FormatRuleEntity(
    @Id
    val id: Int,
    @Column(name = "format_rule", nullable = false, unique = true, length = 100)
    val formatRule: String,
    @Column(name = "default_value")
    val defaultValue: Int? = null,
)
