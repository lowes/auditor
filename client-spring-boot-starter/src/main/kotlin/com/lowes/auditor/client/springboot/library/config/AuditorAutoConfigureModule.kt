package com.lowes.auditor.client.springboot.library.config

import com.lowes.auditor.client.api.Auditor
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuditorAutoConfig::class)
class AuditorAutoConfigureModule(
    private val auditorAutoConfig: AuditorAutoConfig
) {

    @Bean
    @ConditionalOnMissingBean
    fun auditorServiceScheduler(): Scheduler {
        return Schedulers.newParallel("auditorServiceScheduler")
    }

    @Bean
    fun auditor(elementFilters: List<AuditEventElementFilter>, auditorServiceScheduler: Scheduler): Auditor {
        return Auditor.getInstance(
            producerConfig = auditorAutoConfig.producer,
            auditorEventConfig = auditorAutoConfig.config,
            elementFilters = elementFilters,
            auditorServiceScheduler = auditorServiceScheduler
        )
    }
}
