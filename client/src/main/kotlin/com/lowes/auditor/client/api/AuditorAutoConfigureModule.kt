package com.lowes.auditor.client.api

import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.library.config.AuditorConfig
import com.lowes.auditor.client.library.service.AuditEventGeneratorService
import com.lowes.auditor.client.usecase.ElementAggregatorUseCase
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuditorConfig::class)
internal class AuditorAutoConfigureModule(
    private val auditorConfig: AuditorConfig
) {

    @Bean
    fun elementAggregatorUseCase(objectDiffChecker: ObjectDiffChecker): ElementAggregatorUseCase {
        return ElementAggregatorUseCase(objectDiffChecker)
    }

    @Bean
    fun auditEventGeneratorService(
        elementAggregatorUseCase: ElementAggregatorUseCase,
        auditEventProducerService: EventPublisher
    ): AuditEventGeneratorService {
        return AuditEventGeneratorService(elementAggregatorUseCase, auditorConfig, auditEventProducerService)
    }

    @Bean
    fun auditor(auditEventGeneratorService: AuditEventGeneratorService): Auditor {
        return AuditorService(auditEventGeneratorService)
    }
}
