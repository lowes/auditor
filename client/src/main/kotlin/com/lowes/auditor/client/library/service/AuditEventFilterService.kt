package com.lowes.auditor.client.library.service

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventFilter
import com.lowes.auditor.core.entities.domain.AuditEvent
import reactor.core.publisher.Flux

internal class AuditEventFilterService(
    private val eventFilterUseCase: AuditEventFilter,
    private val elementFilterUseCase: AuditEventElementFilter,
    private val loggingFilterUseCase: AuditEventFilter,
) {

    fun filter(events: Flux<AuditEvent>, auditorEventConfig: AuditorEventConfig): Flux<AuditEvent> {
        return Flux.deferContextual { context -> events.map { context to it } }
            .filter { eventFilterUseCase.filter(it.first, it.second, auditorEventConfig.filters) }
            .map {
                val event = it.second
                it.first to
                    event.copy(elements = elementFilterUseCase.filter(event.elements, auditorEventConfig.filters?.element))
            }
            .filter { it.second.elements.isNotEmpty() }
            .filter { loggingFilterUseCase.filter(it.first, it.second, auditorEventConfig.filters) }
            .map { it.second }
    }
}
