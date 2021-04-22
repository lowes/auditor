package com.lowes.auditor.client.entities.interfaces.usecase

import com.lowes.auditor.client.entities.domain.Filters
import com.lowes.auditor.core.entities.domain.AuditEvent
import reactor.util.context.ContextView

interface AuditEventFilter {

    fun filter(context: ContextView, event: AuditEvent, filters: Filters?): Boolean
}
