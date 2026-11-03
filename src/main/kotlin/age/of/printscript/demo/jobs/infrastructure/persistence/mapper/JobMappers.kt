package age.of.printscript.demo.jobs.infrastructure.persistence.mapper

import age.of.printscript.demo.jobs.domain.Job
import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.jobs.domain.JobType
import age.of.printscript.demo.jobs.infrastructure.persistence.entity.JobEntity
import age.of.printscript.demo.snippet.domain.SnippetId
import age.of.printscript.demo.snippet.domain.UserId
import java.util.UUID

fun JobEntity.toDomain(): Job =
    Job(
        id = JobId(id.toString()),
        snippetId = SnippetId(snippetId.toString()),
        snippetVersion = snippetVersion,
        userId = UserId(userId),
        type = JobType(type),
        status = JobStatus.valueOf(status),
        result = result,
    )

fun Job.toEntity(): JobEntity =
    JobEntity(
        id = UUID.fromString(id.value),
        snippetId = UUID.fromString(snippetId.value),
        snippetVersion = snippetVersion,
        userId = userId.value,
        type = type.value,
        status = status.name,
        result = result,
    )
