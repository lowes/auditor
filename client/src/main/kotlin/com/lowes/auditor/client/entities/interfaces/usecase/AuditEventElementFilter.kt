package com.lowes.auditor.client.entities.interfaces.usecase

import com.lowes.auditor.client.entities.domain.Element
import com.lowes.auditor.client.entities.domain.ElementFilter

interface AuditEventElementFilter {

    fun filter(elements: List<Element>, elementFilter: ElementFilter?): List<Element>
}
