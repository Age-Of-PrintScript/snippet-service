package age.of.printscript.demo

import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.springframework.web.bind.annotation.RestController

@AnalyzeClasses(packages = ["age.of.printscript.demo"])
class ArchitectureTest {
    @JvmField
    @ArchTest
    val `clean architecture layers should be respected`: ArchRule =
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("Domain")
            .definedBy("..domain..")
            .layer("Application")
            .definedBy("..application..")
            .layer("Infrastructure")
            .definedBy("..infrastructure..")
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers(
                "Application",
                "Infrastructure",
            ).whereLayer("Application")
            .mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Infrastructure")
            .mayNotBeAccessedByAnyLayer()

    @JvmField
    @ArchTest
    val `domain should not depend on external frameworks`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "com.fasterxml.jackson..",
            )

    @JvmField
    @ArchTest
    val `controllers should reside in infrastructure`: ArchRule =
        classes()
            .that()
            .areAnnotatedWith(RestController::class.java)
            .should()
            .resideInAPackage("..infrastructure..")
}
