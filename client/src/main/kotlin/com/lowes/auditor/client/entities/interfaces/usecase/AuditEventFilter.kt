package com.lowes.auditor.client.entities.interfaces.usecase

import com.lowes.auditor.client.entities.domain.Filters
import com.lowes.auditor.core.entities.domain.AuditEvent
import reactor.util.context.ContextView

/**
 * Audit event filter interface containing functions to filter audit events
 */
interface AuditEventFilter {

    /**
     * Filters audit events based on the given [filters]
     * @param context an instance of [ContextView] containing context relevant metadata
     * @param event instance of [AuditEvent]
     * @param filters instance of type [Filters]
     * @return boolean if the said [event] needs to be filtered out based on existing [filters]
     */
    fun filter(context: ContextView, event: AuditEvent, filters: Filters?): Boolean
}
