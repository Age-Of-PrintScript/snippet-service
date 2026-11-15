package age.of.printscript.demo.testing.infrastructure.persistence.mapper

import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import age.of.printscript.demo.testing.domain.TestRun
import age.of.printscript.demo.testing.domain.TestRunId
import age.of.printscript.demo.testing.domain.TestRunStatus
import age.of.printscript.demo.testing.infrastructure.persistence.entity.TestRunEntity
import java.util.UUID

fun TestRunEntity.toDomain(): TestRun =
    TestRun(
        id = TestRunId(id.toString()),
        snippetId = SnippetId(snippetId.toString()),
        snippetVersion = snippetVersion,
        requestedBy = UserId(requestedBy),
        status = TestRunStatus.valueOf(status),
        createdAt = createdAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
    )

fun TestRun.toEntity(): TestRunEntity =
    TestRunEntity(
        id = UUID.fromString(id.value),
        snippetId = UUID.fromString(snippetId.value),
        snippetVersion = snippetVersion,
        requestedBy = requestedBy.value,
        status = status.name,
        createdAt = createdAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
    )
