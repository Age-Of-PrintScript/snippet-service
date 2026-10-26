package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.LintStatus
import age.of.printscript.demo.domain.LintWarning
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetLintStatus
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetLintStatusEntity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.UUID

private val objectMapper = jacksonObjectMapper()

fun SnippetLintStatusEntity.toDomain(): SnippetLintStatus =
    SnippetLintStatus(
        snippetId = SnippetId(snippetId.toString()),
        version = version,
        status = LintStatus.valueOf(status),
        warnings = deserializeWarnings(warnings),
    )

private fun deserializeWarnings(warnings: String?): List<LintWarning> =
    warnings
        ?.takeIf { it.isNotBlank() }
        ?.let { objectMapper.readValue<List<LintWarning>>(it) }
        ?: emptyList()

fun SnippetLintStatus.toEntity(): SnippetLintStatusEntity =
    SnippetLintStatusEntity(
        snippetId = UUID.fromString(snippetId.value),
        version = version,
        status = status.name,
        warnings = objectMapper.writeValueAsString(warnings),
    )
