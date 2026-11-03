package age.of.printscript.demo.infrastructure.persistance.mapper

import age.of.printscript.demo.domain.Job
import age.of.printscript.demo.domain.JobId
import age.of.printscript.demo.domain.JobStatus
import age.of.printscript.demo.domain.JobType
import age.of.printscript.demo.domain.SnippetId
import age.of.printscript.demo.domain.UserId
import age.of.printscript.demo.infrastructure.persistance.entity.JobEntity
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
