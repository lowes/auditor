package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.Filters
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventFilter
import com.lowes.auditor.core.entities.domain.AuditEvent
import reactor.util.context.ContextView

/**
 * Provides implementation of [AuditEventFilter] for filtering [AuditEvent] based on [Filters]
 * @see AuditEventFilter
 */
class EventFilterUseCase : AuditEventFilter {
    /**
     * Filters audit events based on [Filters]
     * @see AuditEventFilter.filter
     */
    override fun filter(context: ContextView, event: AuditEvent, filters: Filters?): Boolean {
        val eventFilter = filters?.event
        return if (eventFilter?.enabled == true) {
            if (eventFilter.type.isNullOrEmpty()) {
                true
            } else {
                eventFilter.type.contains(event.type)
            }
        } else {
            true
        }
    }
}
