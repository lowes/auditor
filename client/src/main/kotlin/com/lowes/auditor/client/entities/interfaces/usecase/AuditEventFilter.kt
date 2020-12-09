package com.lowes.auditor.client.entities.interfaces.usecase

import com.lowes.auditor.client.entities.domain.AuditEvent
import com.lowes.auditor.client.entities.domain.Filters

internal interface AuditEventFilter {

    fun filter(event: AuditEvent, filters: Filters?): Boolean
}
