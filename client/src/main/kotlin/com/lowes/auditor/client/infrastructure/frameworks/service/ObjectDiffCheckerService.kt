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

    /**
     * provides diff between two objects
     * @see ObjectDiffChecker.diff
     */
    override fun diff(objectOne: Any?, objectTwo: Any?): Flux<Element> {
        return when {
            objectOne == null && objectTwo != null -> getElementsWhenSingleObjectExists(objectTwo, CREATED)
            objectOne != null && objectTwo == null -> getElementsWhenSingleObjectExists(objectOne, DELETED)
            objectOne != null && objectTwo != null -> getElementsWhenBothObjectExists(objectOne, objectTwo)
            else -> Flux.empty()
        }
    }

    /**
     * Converts the difference between two object's properties into flux of [Element]
     */
    private fun getElementsWhenBothObjectExists(objectOne: Any, objectTwo: Any): Flux<Element> {
        val objectOneElements = getElementsWhenSingleObjectExists(objectOne, DELETED)
        val objectTwoElements = getElementsWhenSingleObjectExists(objectTwo, CREATED)
        return Flux.merge(objectOneElements, objectTwoElements)
            .groupBy({ it.metadata?.fqdn }, auditorEventConfig.maxElements?.times(TWO).orDefault(ONE_THOUSAND))
            .flatMap {
                it.reduce { left, right ->
                    when {
                        left != null && right == null -> left
                        right != null && left == null -> right
                        left?.previousValue != right?.updatedValue -> left.copy(updatedValue = right.updatedValue)
                        else -> Element()
                    }
                }
            }
            .filter { it.name != null }
    }

    /**
     * Converts a single object properties into flux of [Element]
     */
    private fun getElementsWhenSingleObjectExists(singleObject: Any, eventType: EventType, fqcn: String? = null): Flux<Element> {
        val node = objectMapper.valueToTree<JsonNode>(singleObject)
        return JsonNodeMapper.toElement(node, eventType, fqcn ?: singleObject.javaClass.canonicalName)
    }
}
