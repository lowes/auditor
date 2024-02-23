package com.lowes.auditor.client.entities.interfaces.usecase

import com.lowes.auditor.client.entities.domain.ElementFilter
import com.lowes.auditor.core.entities.domain.Element

/**
 * AuditEvent Element Filter interface containing functions to filter audit event's elements.
 */
interface AuditEventElementFilter {
    /**
     * Filters out individual element from the list of [Element] based on [ElementFilter] conditions
     * @param elements list of [Element]
     * @param elementFilter instance of type [ElementFilter]
     * @return filtered list of [Element]
     */
    fun filter(
        elements: List<Element>,
        elementFilter: ElementFilter?,
    ): List<Element>
}
