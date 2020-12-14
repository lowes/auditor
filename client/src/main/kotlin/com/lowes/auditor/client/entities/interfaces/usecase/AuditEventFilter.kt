package com.lowes.auditor.client.entities.interfaces.usecase

import com.lowes.auditor.client.entities.domain.Filters
import com.lowes.auditor.core.entities.domain.AuditEvent

internal interface AuditEventFilter {

    fun filter(event: AuditEvent, filters: Filters?): Boolean
}
