package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.ElementFilter
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.entities.util.getOrNull
import com.lowes.auditor.client.entities.util.orDefault
import com.lowes.auditor.core.entities.domain.Element
import java.util.stream.Collectors
import kotlin.streams.toList

/**
 * Provides implementation of [AuditEventElementFilter] for including/excluding elements from audit events
 * @property elementFilters list of [AuditEventElementFilter]
 * @see AuditEventElementFilter
 */
class ElementFilterUseCase(
    private val elementFilters: List<AuditEventElementFilter>,
) : AuditEventElementFilter {
    /**
     * Filters individual [elements] based on [elementFilter]
     * @see AuditEventElementFilter.filter
     */
    override fun filter(
        elements: List<Element>,
        elementFilter: ElementFilter?,
    ): List<Element> {
        return if (elementFilter?.enabled == true) {
            elementFilter
                .types
                ?.stream()
                ?.map { typeName ->
                    elementFilters
                        .stream()
                        .filter { it::class.java.simpleName.equals(typeName, ignoreCase = true) }
                        .findFirst()
                        .getOrNull()
                }
                ?.map { it?.filter(elements, elementFilter) }
                ?.reduce { acc, list ->
                    acc?.stream()?.filter { list?.contains(it) == true }?.collect(Collectors.toList())
                }?.get().orEmpty()
        } else {
            elements
        }
    }
}

/**
 * Provides an implementation of [AuditEventElementFilter] for including elements from [Element] list based on configurations in [ElementFilter]
 * @see AuditEventElementFilter
 */
class InclusionFilter : AuditEventElementFilter {
    /**
     * Filters individual [elements] and perform an inclusion rule as mentioned in [elementFilter]
     * @see AuditEventElementFilter.filter
     */
    override fun filter(
        elements: List<Element>,
        elementFilter: ElementFilter?,
    ): List<Element> {
        return elements
            .stream()
            .filter { element ->
                val includedElements = elementFilter?.options?.includes
                includedElements?.stream()?.anyMatch { elementNameFromConfig ->
                    if (elementNameFromConfig.contains(".")) {
                        elementNameFromConfig.contains(element.metadata?.fqdn.orEmpty())
                    } else {
                        elementNameFromConfig.equals(element.name.orEmpty(), ignoreCase = true)
                    }
                }.orDefault(true)
            }.toList()
    }
}

/**
 * Provides an implementation of [AuditEventElementFilter] for excluding elements from [Element] list based on configurations in [ElementFilter]
 * @see AuditEventElementFilter
 */
class ExclusionFilter : AuditEventElementFilter {
    /**
     * Filters individual [elements] and perform an exclusion rule as mentioned in [elementFilter]
     * @see AuditEventElementFilter.filter
     */
    override fun filter(
        elements: List<Element>,
        elementFilter: ElementFilter?,
    ): List<Element> {
        return elements
            .stream()
            .filter { element ->
                val excludedElements = elementFilter?.options?.excludes
                excludedElements?.stream()?.noneMatch { elementNameFromConfig ->
                    if (elementNameFromConfig.contains(".")) {
                        elementNameFromConfig.contains(element.metadata?.fqdn.orEmpty())
                    } else {
                        elementNameFromConfig.equals(element.name.orEmpty(), ignoreCase = true)
                    }
                }.orDefault(true)
            }.toList()
    }
}
