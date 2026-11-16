package age.of.printscript.demo.jobs.application

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.shared.application.UseCase
import age.of.printscript.demo.snippet.application.port.PermissionGateway
import age.of.printscript.demo.snippet.domain.SnippetRepository
import age.of.printscript.demo.snippet.domain.UserId

sealed interface GetJobStatusResult {
    data class Success(
        val status: JobStatus,
    ) : GetJobStatusResult

    data object NotFound : GetJobStatusResult

    data object Forbidden : GetJobStatusResult
}

class GetJobStatus(
    private val jobs: JobRepository,
    private val snippets: SnippetRepository,
    private val permissions: PermissionGateway,
) : UseCase {
    @Suppress("ReturnCount")
    operator fun invoke(
        jobId: JobId,
        userId: UserId,
    ): GetJobStatusResult {
        val job = jobs.findById(jobId) ?: return GetJobStatusResult.NotFound
        val snippet = snippets.findLatest(job.snippetId) ?: return GetJobStatusResult.NotFound
        if (snippet.ownerId != userId && !permissions.hasReadAccess(job.snippetId, userId)) {
            return GetJobStatusResult.Forbidden
        }
        return GetJobStatusResult.Success(job.status)
    }
}
