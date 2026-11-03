package age.of.printscript.demo.domain

@JvmInline
value class JobId(
    val value: String,
)

@JvmInline
value class JobType(
    val value: String,
)

enum class JobStatus {
    PENDING,
    DONE,
    FAILED,
}

data class Job(
    val id: JobId,
    val snippetId: SnippetId,
    val snippetVersion: Int,
    val userId: UserId,
    val type: JobType,
    val status: JobStatus,
    val result: String? = null,
)
