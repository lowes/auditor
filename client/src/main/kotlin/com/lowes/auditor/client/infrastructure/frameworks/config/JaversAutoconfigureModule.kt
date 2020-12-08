package com.lowes.auditor.client.infrastructure.frameworks.config

import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.infrastructure.frameworks.service.JaversWrapperService
import org.javers.core.Javers
import org.javers.core.JaversBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
internal class JaversAutoconfigureModule {
    @Bean
    fun javers(): Javers {
        return JaversBuilder.javers().build()
    }

    @Bean
    fun objectDiffChecker(javers: Javers): ObjectDiffChecker {
        return JaversWrapperService(javers)
    }
}
