package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.SnippetTest
import age.of.printscript.demo.domain.SnippetTestId
import age.of.printscript.demo.domain.SnippetTestSummary
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.SnippetTestEntity
import java.util.UUID

fun SnippetTestEntity.toSummary(): SnippetTestSummary =
    SnippetTestSummary(
        id = SnippetTestId(id.toString()),
        snippetId = SnippetId(snippetId.toString()),
        ownerId = UserId(ownerId),
        createdAt = createdAt,
    )

fun SnippetTestEntity.toDomain(inputs: List<String>, outputs: List<String>): SnippetTest =
    SnippetTest(
        id = SnippetTestId(id.toString()),
        snippetId = SnippetId(snippetId.toString()),
        ownerId = UserId(ownerId),
        inputs = inputs,
        outputs = outputs,
    )

fun SnippetTest.toEntity(url: String): SnippetTestEntity =
    SnippetTestEntity(
        id = UUID.fromString(id.value),
        snippetId = UUID.fromString(snippetId.value),
        ownerId = ownerId.value,
        url = url,
    )
