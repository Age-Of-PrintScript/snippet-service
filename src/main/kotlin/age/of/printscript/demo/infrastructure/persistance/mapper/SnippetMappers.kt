package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.Snippet
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetEntity

import java.util.UUID

fun SnippetEntity.toDomain(content: String): Snippet =
    Snippet(
        id = SnippetId(id.toString()),
        title = title,
        description = description,
        ownerId = UserId(ownerId),
        language = language,
        version = version,
        content = content,
    )

fun Snippet.toEntity(url: String): SnippetEntity =
    SnippetEntity(
        id = UUID.fromString(id.value),
        ownerId = ownerId.value,
        title = title,
        description = description,
        language = language,
        version = version,
        url = url,
    )
