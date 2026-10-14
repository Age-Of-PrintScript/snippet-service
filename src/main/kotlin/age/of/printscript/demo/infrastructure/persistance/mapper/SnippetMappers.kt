package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.Snippet
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetEntity
import java.util.UUID

fun SnippetEntity.toDomain(): Snippet =
    Snippet(
        id = SnippetId(id.toString()),
        version = version,
        title = title,
        description = description,
        ownerId = UserId(ownerId),
        language = language,
        languageVersion = languageVersion,
        contentUrl = url,
        isLatest = isLatest,
    )

fun Snippet.toEntity(): SnippetEntity =
    SnippetEntity(
        id = UUID.fromString(id.value),
        version = version,
        ownerId = ownerId.value,
        title = title,
        description = description,
        language = language,
        languageVersion = languageVersion,
        url = contentUrl,
        isLatest = isLatest,
    )
