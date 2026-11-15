package age.of.printscript.demo.testing.domain

interface TestExecutionRepository {
    fun findById(id: TestExecutionId): TestExecution?

    fun findAllByRunId(runId: TestRunId): List<TestExecution>

    fun findLatestCompleted(
        testId: SnippetTestId,
        snippetVersion: Int,
    ): TestExecution?

    fun save(testExecution: TestExecution): TestExecution
}
