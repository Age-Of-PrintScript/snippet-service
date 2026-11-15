package age.of.printscript.demo.testing.infrastructure.persistence.mapper

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.testing.domain.SnippetTestId
import age.of.printscript.demo.testing.domain.TestExecution
import age.of.printscript.demo.testing.domain.TestExecutionId
import age.of.printscript.demo.testing.domain.TestExecutionStatus
import age.of.printscript.demo.testing.domain.TestRunId
import age.of.printscript.demo.testing.infrastructure.persistence.entity.TestExecutionEntity
import java.util.UUID

fun TestExecutionEntity.toDomain(): TestExecution =
    TestExecution(
        id = TestExecutionId(id.toString()),
        runId = TestRunId(runId.toString()),
        testId = SnippetTestId(testId.toString()),
        jobId = JobId(jobId.toString()),
        testUrl = testUrl,
        status = TestExecutionStatus.valueOf(status),
        isStale = isStale,
        createdAt = createdAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
    )

fun TestExecution.toEntity(): TestExecutionEntity =
    TestExecutionEntity(
        id = UUID.fromString(id.value),
        runId = UUID.fromString(runId.value),
        testId = UUID.fromString(testId.value),
        jobId = UUID.fromString(jobId.value),
        testUrl = testUrl,
        status = status.name,
        isStale = isStale,
        createdAt = createdAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
    )
