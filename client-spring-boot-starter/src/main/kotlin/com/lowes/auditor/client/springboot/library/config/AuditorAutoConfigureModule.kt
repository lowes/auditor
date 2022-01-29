package com.lowes.auditor.client.springboot.library.config

import com.lowes.auditor.client.api.Auditor
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers

/**
 * Spring boot's autoconfigure module to initialize and load [Auditor] instances
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuditorAutoConfig::class)
class AuditorAutoConfigureModule(
    private val auditorAutoConfig: AuditorAutoConfig
) {

    /**
     * Creates a [Scheduler] instance exclusively for managing audits
     * @return a reactor scheduler instance
     */
    @Bean
    @ConditionalOnMissingBean
    fun auditorServiceScheduler(): Scheduler {
        return Schedulers.newParallel("auditorServiceScheduler")
    }

    /**
     * Creates an [Auditor] instance that is injected to application spring lifecycle
     * @param elementFilters list of [AuditEventElementFilter]
     * @param auditorServiceScheduler initialized auditor Scheduler
     * @return an [Auditor] instance
     */
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
