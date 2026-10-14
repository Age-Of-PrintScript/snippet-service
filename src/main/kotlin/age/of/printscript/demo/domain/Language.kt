package age.of.printscript.demo.domain

interface Language {
    fun validate(
        content: String,
        version: String,
    ): ValidationResult

    fun format(
        bucketUrl: String,
        version: String,
        rules: FormatRules,
    ): String

    fun lint(
        bucketUrl: String,
        version: String,
        rules: LintRules,
    ): LintResult

    fun execute(
        bucketUrl: String,
        version: String,
        inputs: List<String>,
    ): ExecutionResult
}
