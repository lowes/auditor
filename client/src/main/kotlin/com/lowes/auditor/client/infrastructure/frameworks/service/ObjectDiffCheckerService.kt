package com.lowes.auditor.client.infrastructure.frameworks.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.entities.util.ONE_THOUSAND
import com.lowes.auditor.client.entities.util.TWO
import com.lowes.auditor.client.entities.util.orDefault
import com.lowes.auditor.client.infrastructure.frameworks.mapper.JsonNodeMapper
import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.EventType
import com.lowes.auditor.core.entities.domain.EventType.CREATED
import com.lowes.auditor.core.entities.domain.EventType.DELETED
import reactor.core.publisher.Flux

/**
 * Provides implementation definitions for [ObjectDiffChecker]
 * @property objectMapper instance of [ObjectMapper]
 * @property auditorEventConfig instance of [AuditorEventConfig]
 * @see ObjectDiffChecker
 */
class ObjectDiffCheckerService(
    private val objectMapper: ObjectMapper,
    private val auditorEventConfig: AuditorEventConfig
) : ObjectDiffChecker {

    private val prefetch = auditorEventConfig.maxElements?.times(TWO).orDefault(ONE_THOUSAND)
    private val ignoreCollectionOrder = auditorEventConfig.ignoreCollectionOrder?.enabled.orDefault(false)
    private val altIdentifierFields = auditorEventConfig.ignoreCollectionOrder?.fields.orDefault(listOf("id"))

    /**
     * provides diff between two objects
     * @see ObjectDiffChecker.diff
     */
    override fun diff(objectOne: Any?, objectTwo: Any?): Flux<Element> {
        return when {
            objectOne == null && objectTwo != null -> getElementsWhenSingleObjectExists(objectTwo, CREATED)
            objectOne != null && objectTwo == null -> getElementsWhenSingleObjectExists(objectOne, DELETED)
            objectOne != null && objectTwo != null -> getElementsWhenBothObjectsExist(objectOne, objectTwo)
            else -> Flux.empty()
        }
    }

    /**
     * Converts the difference between two object's properties into flux of [Element]
     */
    private fun getElementsWhenBothObjectsExist(objectOne: Any, objectTwo: Any): Flux<Element> {
        val objectOneElements = getElementsWhenSingleObjectExists(objectOne, DELETED)
        val objectTwoElements = getElementsWhenSingleObjectExists(objectTwo, CREATED)
        return Flux.merge(objectOneElements, objectTwoElements)
            .groupBy({ it.metadata?.fqdn ?: "missingMetaData" }, prefetch)
            .flatMap { it.collectList().flatMapIterable { list -> getChanges(list) } }
            .filter { it.name != null }
    }

    /**
     * Converts a single object properties into flux of [Element]
     */
    private fun getElementsWhenSingleObjectExists(singleObject: Any, eventType: EventType, fqcn: String? = null): Flux<Element> {
        val node = objectMapper.valueToTree<JsonNode>(singleObject)
        return JsonNodeMapper.toElement(node, eventType, fqcn ?: singleObject.javaClass.canonicalName, ignoreCollectionOrder, altIdentifierFields)
    }

    /**
     * Checks list of elements with same fqdn for changes
     */
    private fun getChanges(elements: List<Element>): List<Element> {
        val changes = mutableListOf<Element>()
        // typical create/delete
        if (elements.size == 1) changes.add(elements[0])
        // typical update, not sure if order will be consistent so check both ways
        else if (elements.size == 2) {
            if (elements[0].previousValue != null && elements[0].previousValue != elements[1].updatedValue)
                changes.add(elements[0].copy(updatedValue = elements[1].updatedValue))
            else if (elements[1].previousValue != null && elements[1].previousValue != elements[0].updatedValue)
                changes.add(elements[1].copy(updatedValue = elements[0].updatedValue))
        }
        // will only be reached if ignoring order is enabled and a collection contains duplicate primitives
        // since all values will be the same, we just need to compare the number of previous/updated
        else {
            val previous = mutableListOf<Element>()
            val updated = mutableListOf<Element>()
            elements.forEach { element ->
                if (element.previousValue != null) previous.add(element)
                else if (element.updatedValue != null) updated.add(element)
            }
            if (previous.size > updated.size) changes.addAll(previous.drop(updated.size))
            else if (updated.size > previous.size) changes.addAll(updated.drop(previous.size))
        }
        return changes
    }
}
