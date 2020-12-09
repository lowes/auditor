package com.lowes.auditor.client.library.service

import com.lowes.auditor.client.entities.domain.AuditEvent
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventFilter
import reactor.core.publisher.Flux

internal class AuditEventFilterService(
    private val eventFilterUseCase: AuditEventFilter,
    private val elementFilterUseCase: AuditEventElementFilter,
    private val loggingFilterUseCase: AuditEventFilter,
) {

    fun filter(events: Flux<AuditEvent>, auditorEventConfig: AuditorEventConfig): Flux<AuditEvent> {
        return events.filter {
            eventFilterUseCase.filter(it, auditorEventConfig.filters)
        }
            .map {
                it.copy(elements = elementFilterUseCase.filter(it.elements, auditorEventConfig.filters?.element))
            }
            .filter { it.elements.isNotEmpty() }
            .filter {
                loggingFilterUseCase.filter(it, auditorEventConfig.filters)
            }
    }
}
