package com.lowes.auditor.client.library.service

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.util.merge
import com.lowes.auditor.client.usecase.ElementAggregatorUseCase
import com.lowes.auditor.core.entities.domain.AuditEvent
import reactor.core.publisher.Flux

class AuditEventGeneratorService(
    private val elementAggregatorUseCase: ElementAggregatorUseCase,
    private val initialConfig: AuditorEventConfig,
    private val eventPublisher: EventPublisher,
    private val auditEventFilterService: AuditEventFilterService,
    private val auditEventDecoratorService: AuditEventDecoratorService,
) {
    fun audit(oldObject: Any?, newObject: Any?, requestConfig: AuditorEventConfig?): Flux<AuditEvent> {
        val config = requestConfig?.let { initialConfig merge it } ?: initialConfig
        val events = elementAggregatorUseCase.aggregate(oldObject, newObject, config)
        val decoratedEvents = auditEventDecoratorService.decorate(events, config, newObject)
        val filteredEvents = auditEventFilterService.filter(decoratedEvents, config)
        return eventPublisher.publishEvents(filteredEvents).flatMap { events }
    }
}
