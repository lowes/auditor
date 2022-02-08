package com.lowes.auditor.client.library.service

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventFilter
import com.lowes.auditor.core.entities.domain.AuditEvent
import reactor.core.publisher.Flux

/**
 * Audit event filter service that orchestrates the [AuditEvent] flow through various [AuditEventFilter] and [AuditEventElementFilter]
 * @property eventFilterUseCase type of [AuditEventFilter] which applies filter logic based on audit event
 * @property elementFilterUseCase type of [AuditEventElementFilter] which applies filter logic based on audit event's elements
 * @property loggingFilterUseCase type of [AuditEventElementFilter] which applies logging filter based on audit event
 */
class AuditEventFilterService(
    private val eventFilterUseCase: AuditEventFilter,
    private val elementFilterUseCase: AuditEventElementFilter,
    private val loggingFilterUseCase: AuditEventFilter,
) {

    /**
     * Filters flux of [AuditEvent] based all existing filters and configurations present in [auditorEventConfig]
     * @param events flux of [AuditEvent]
     * @param auditorEventConfig instance of [AuditorEventConfig]
     * @return filtered flux of [AuditEvent]
     */
    fun filter(events: Flux<AuditEvent>, auditorEventConfig: AuditorEventConfig): Flux<AuditEvent> {
        return Flux.deferContextual { context -> events.map { context to it } }
            .filter { eventFilterUseCase.filter(it.first, it.second, auditorEventConfig.filters) }
            .map {
                val event = it.second
                it.first to
                    event.copy(elements = elementFilterUseCase.filter(event.elements.orEmpty(), auditorEventConfig.filters?.element))
            }
            .filter { !it.second.log.isNullOrEmpty() || !it.second.elements.isNullOrEmpty() }
            .filter { loggingFilterUseCase.filter(it.first, it.second, auditorEventConfig.filters) }
            .map { it.second }
    }
}
