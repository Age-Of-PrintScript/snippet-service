package age.of.printscript.demo.testing.application.port

class TestJobPublicationException(
    cause: Throwable,
) : RuntimeException("Could not publish test job", cause)
