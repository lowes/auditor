package com.lowes.auditor.client.library.service

import com.lowes.auditor.client.entities.domain.AuditEvent
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.library.config.AuditorConfig
import com.lowes.auditor.client.usecase.ElementAggregatorUseCase
import reactor.core.publisher.Flux
import javax.inject.Inject
import javax.inject.Named

@Named
internal class AuditEventGeneratorService @Inject constructor(
    private val elementAggregatorUseCase: ElementAggregatorUseCase,
    private val auditorConfig: AuditorConfig,
    private val eventPublisher: EventPublisher,
) {
    fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig): Flux<AuditEvent> {
        val config = auditorEventConfig.copyIfNull(auditorConfig.config)
        val events = elementAggregatorUseCase
            .aggregate(oldObject, newObject, config)
        return eventPublisher
            .publishEvents(events)
            .flatMap { events }
    }
}
