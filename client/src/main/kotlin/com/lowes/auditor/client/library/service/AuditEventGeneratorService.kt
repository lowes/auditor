package com.lowes.auditor.client.library.service

import com.lowes.auditor.client.entities.domain.AuditEvent
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.util.merge
import com.lowes.auditor.client.library.config.AuditorConfig
import com.lowes.auditor.client.usecase.ElementAggregatorUseCase
import reactor.core.publisher.Flux

internal class AuditEventGeneratorService(
    private val elementAggregatorUseCase: ElementAggregatorUseCase,
    private val auditorConfig: AuditorConfig,
    private val eventPublisher: EventPublisher,
    private val auditEventFilterService: AuditEventFilterService,
    private val auditEventDecoratorService: AuditEventDecoratorService,
) {
    fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?): Flux<AuditEvent> {
        val config = auditorEventConfig?.let { auditorConfig.config merge it } ?: auditorConfig.config
        val events = elementAggregatorUseCase.aggregate(oldObject, newObject, config)
        val decoratedEvents = auditEventDecoratorService.decorate(events, config, newObject)
        val filteredEvents = auditEventFilterService.filter(decoratedEvents, config)
        return eventPublisher.publishEvents(filteredEvents).flatMap { events }
    }
}
