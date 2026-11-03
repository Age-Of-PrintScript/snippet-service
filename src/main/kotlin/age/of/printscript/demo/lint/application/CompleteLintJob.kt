package age.of.printscript.demo.lint.application

import age.of.printscript.demo.jobs.domain.JobId
import age.of.printscript.demo.jobs.domain.JobRepository
import age.of.printscript.demo.jobs.domain.JobStatus
import age.of.printscript.demo.lint.domain.LintStatus
import age.of.printscript.demo.lint.domain.LintWarning
import age.of.printscript.demo.lint.domain.SnippetLintStatus
import age.of.printscript.demo.lint.domain.SnippetLintStatusRepository
import age.of.printscript.demo.shared.application.UseCase

class CompleteLintJob(
    private val snippetLintStatusRepository: SnippetLintStatusRepository,
    private val jobRepository: JobRepository,
) : UseCase {
    operator fun invoke(
        jobId: JobId,
        warnings: List<LintWarning>,
    ) {
        val job = jobRepository.findById(jobId) ?: return
        val lintStatus =
            if (warnings.isEmpty()) {
                LintStatus.COMPLIANT
            } else {
                LintStatus.NON_COMPLIANT
            }
        jobRepository.save(job.copy(status = JobStatus.DONE))
        snippetLintStatusRepository.save(
            SnippetLintStatus(
                snippetId = job.snippetId,
                version = job.snippetVersion,
                status = lintStatus,
                warnings = warnings,
            ),
        )
    }
}
