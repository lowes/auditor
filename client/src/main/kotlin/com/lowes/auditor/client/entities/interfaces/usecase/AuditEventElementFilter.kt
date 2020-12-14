package com.lowes.auditor.client.entities.interfaces.usecase

import com.lowes.auditor.client.entities.domain.ElementFilter
import com.lowes.auditor.core.entities.domain.Element

interface AuditEventElementFilter {

    fun filter(elements: List<Element>, elementFilter: ElementFilter?): List<Element>
}
