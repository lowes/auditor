package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.ElementFilter
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.entities.util.getOrNull
import com.lowes.auditor.client.entities.util.orDefault
import com.lowes.auditor.core.entities.domain.Element
import java.util.stream.Collectors
import kotlin.streams.toList

class ElementFilterUseCase(
    private val elementFilters: List<AuditEventElementFilter>
) : AuditEventElementFilter {

    override fun filter(elements: List<Element>, elementFilterConfig: ElementFilter?): List<Element> {
        return if (elementFilterConfig?.enabled == true) {
            elementFilterConfig
                .types
                ?.stream()
                ?.map { typeName ->
                    elementFilters
                        .stream()
                        .filter { it::class.java.simpleName.equals(typeName, ignoreCase = true) }
                        .findFirst()
                        .getOrNull()
                }
                ?.map { it?.filter(elements, elementFilterConfig) }
                ?.reduce { acc, list ->
                    acc?.stream()?.filter { list?.contains(it) == true }?.collect(Collectors.toList())
                }?.get().orEmpty()
        } else {
            elements
        }
    }
}

class InclusionFilter : AuditEventElementFilter {

    override fun filter(elements: List<Element>, elementFilter: ElementFilter?): List<Element> {
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

class ExclusionFilter : AuditEventElementFilter {

    override fun filter(elements: List<Element>, elementFilter: ElementFilter?): List<Element> {
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
