package com.lowes.auditor.client.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventFilter
import com.lowes.auditor.client.library.config.AuditorConfig
import com.lowes.auditor.client.library.service.AuditEventDecoratorService
import com.lowes.auditor.client.library.service.AuditEventFilterService
import com.lowes.auditor.client.library.service.AuditEventGeneratorService
import com.lowes.auditor.client.usecase.ElementAggregatorUseCase
import com.lowes.auditor.client.usecase.ElementFilterUseCase
import com.lowes.auditor.client.usecase.EventFilterUseCase
import com.lowes.auditor.client.usecase.ExclusionFilter
import com.lowes.auditor.client.usecase.InclusionFilter
import com.lowes.auditor.client.usecase.LoggingFilterUseCase
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
    fun inclusionFilter(): AuditEventElementFilter {
        return InclusionFilter()
    }

    @Bean
    fun exclusionFilter(): AuditEventElementFilter {
        return ExclusionFilter()
    }

    @Bean
    fun elementFilterUseCase(elementFilters: List<AuditEventElementFilter>): AuditEventElementFilter {
        return ElementFilterUseCase(elementFilters)
    }

    @Bean
    fun eventFilterUseCase(): AuditEventFilter {
        return EventFilterUseCase()
    }

    @Bean
    fun loggingFilterUseCase(): AuditEventFilter {
        return LoggingFilterUseCase()
    }

    @Bean
    fun auditEventFilterService(
        eventFilterUseCase: AuditEventFilter,
        elementFilterUseCase: AuditEventElementFilter,
        loggingFilterUseCase: AuditEventFilter
    ): AuditEventFilterService {
        return AuditEventFilterService(eventFilterUseCase, elementFilterUseCase, loggingFilterUseCase)
    }

    @Bean
    fun auditEventDecoratorService(auditorObjectMapper: ObjectMapper): AuditEventDecoratorService {
        return AuditEventDecoratorService(auditorObjectMapper)
    }

    @Bean
    fun auditEventGeneratorService(
        elementAggregatorUseCase: ElementAggregatorUseCase,
        auditEventProducerService: EventPublisher,
        auditEventFilterService: AuditEventFilterService,
        auditEventDecoratorService: AuditEventDecoratorService,
    ): AuditEventGeneratorService {
        return AuditEventGeneratorService(
            elementAggregatorUseCase,
            auditorConfig,
            auditEventProducerService,
            auditEventFilterService,
            auditEventDecoratorService
        )
    }

    @Bean
    fun auditor(auditEventGeneratorService: AuditEventGeneratorService): Auditor {
        return AuditorService(auditEventGeneratorService)
    }
}
