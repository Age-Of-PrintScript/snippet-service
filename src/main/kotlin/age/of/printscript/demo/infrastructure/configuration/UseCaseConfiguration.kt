package age.of.printscript.demo.infrastructure.configuration

import age.of.printscript.demo.application.UseCase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ComponentScan(
    basePackages = ["age.of.printscript.demo.application"],
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [UseCase::class],
        ),
    ],
)
class UseCaseConfiguration
