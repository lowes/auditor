package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.Filters
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventFilter
import com.lowes.auditor.core.entities.domain.AuditEvent

internal class EventFilterUseCase : AuditEventFilter {
    override fun filter(event: AuditEvent, filters: Filters?): Boolean {
        val eventFilter = filters?.event
        return if (eventFilter?.enabled == true) {
            if (eventFilter.type.isNullOrEmpty()) {
                true
            } else {
                eventFilter.type?.contains(event.type) == true
            }
        } else {
            true
        }
    }
}
