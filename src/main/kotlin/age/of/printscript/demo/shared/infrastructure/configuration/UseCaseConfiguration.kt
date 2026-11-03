package age.of.printscript.demo.shared.infrastructure.configuration

import age.of.printscript.demo.shared.application.UseCase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ComponentScan(
    basePackages = ["age.of.printscript.demo"],
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [UseCase::class],
        ),
    ],
)
class UseCaseConfiguration
