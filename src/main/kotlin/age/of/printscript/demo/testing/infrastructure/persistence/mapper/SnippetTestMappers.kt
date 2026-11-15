package age.of.printscript.demo.testing.infrastructure.persistence.mapper

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.SnippetTest
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.SnippetTestSummary
import age.of.printscript.demo.testing.infrastructure.persistence.entity.SnippetTestEntity
import java.util.UUID

fun SnippetTestEntity.toSummary(): SnippetTestSummary =
    SnippetTestSummary(
        id = SnippetTestId(id.toString()),
        snippetId = SnippetId(snippetId.toString()),
        ownerId = UserId(ownerId),
        createdAt = createdAt,
        contentUrl = url,
    )

fun SnippetTestEntity.toDomain(): SnippetTest =
    SnippetTest(
        id = SnippetTestId(id.toString()),
        snippetId = SnippetId(snippetId.toString()),
        ownerId = UserId(ownerId),
        contentUrl = url,
    )

fun SnippetTest.toEntity(): SnippetTestEntity =
    SnippetTestEntity(
        id = UUID.fromString(id.value),
        snippetId = UUID.fromString(snippetId.value),
        ownerId = ownerId.value,
        url = contentUrl,
    )
