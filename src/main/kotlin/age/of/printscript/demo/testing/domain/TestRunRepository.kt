package age.of.printscript.demo.testing.domain

interface TestRunRepository {
    fun findById(id: TestRunId): TestRun?

    fun save(testRun: TestRun): TestRun
}
