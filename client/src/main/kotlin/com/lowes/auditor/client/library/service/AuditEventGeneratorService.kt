package com.lowes.auditor.client.library.service

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.util.merge
import com.lowes.auditor.client.usecase.ElementAggregatorUseCase
import com.lowes.auditor.client.usecase.EventLogUseCase
import com.lowes.auditor.client.usecase.LoggingFilterUseCase
import com.lowes.auditor.core.entities.domain.AuditEvent
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

class AuditEventGeneratorService(
    private val elementAggregatorUseCase: ElementAggregatorUseCase,
    private val initialConfig: AuditorEventConfig,
    private val eventPublisher: EventPublisher,
    private val auditEventFilterService: AuditEventFilterService,
    private val auditEventDecoratorService: AuditEventDecoratorService,
    private val eventLogUseCase: EventLogUseCase,
) {
    private val logger = LoggerFactory.getLogger(LoggingFilterUseCase::class.java)

    fun audit(oldObject: Any?, newObject: Any?, requestConfig: AuditorEventConfig?): Flux<AuditEvent> {
        val config = requestConfig?.let { initialConfig merge it } ?: initialConfig
        val events = elementAggregatorUseCase.aggregate(oldObject, newObject, config)
        val decoratedEvents = auditEventDecoratorService.decorate(events, config, newObject)
        val filteredEvents = auditEventFilterService.filter(decoratedEvents, config)
        return eventPublisher.publishEvents(filteredEvents)
            .flatMap { events }
            .doOnError { logger.error("audit event failed", it) }
    }

    fun log(entity: Any, requestConfig: AuditorEventConfig?): Flux<AuditEvent> {
        val config = requestConfig?.let { initialConfig merge it } ?: initialConfig
        val events = Flux.from(eventLogUseCase.logEvent(entity, config))
        val decoratedEvents = auditEventDecoratorService.decorate(events, config, entity)
        val filteredEvents = auditEventFilterService.filter(decoratedEvents, config)
        return eventPublisher.publishEvents(filteredEvents)
            .flatMap { events }
            .doOnError { logger.error("audit log event failed", it) }
    }
}
