package com.lowes.auditor.client.infrastructure.frameworks.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.lowes.auditor.client.infrastructure.frameworks.model.NodeType
import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.ElementMetadata
import com.lowes.auditor.core.entities.domain.EventType
import reactor.core.publisher.Flux

/**
 * Provides functionality to parse a [JsonNode] and map it to equivalent flux of [Element]
 */
object JsonNodeMapper {

    /**
     * Converts [JsonNode] to flux of [Element]. It recursively iterates through all object properties
     * and used [EventType] to determine the type of [Element] to generate
     * @param node instance of type [JsonNode]
     * @param eventType instance of type [EventType]
     * @param fqcn fully qualified class name
     * @return flux of [Element]
     */
    fun toElement(
        node: JsonNode,
        eventType: EventType,
        fqcn: String,
        ignoreCollectionOrder: Boolean,
        altIdentifierFields: List<String>
    ): Flux<Element> {
        val isObjectNode = node.fields().hasNext()
        return Flux.fromIterable(getIterables(isObjectNode, node))
            .index()
            .flatMap { indexEntryPair ->
                val index = indexEntryPair.t1.toString()
                val entry = indexEntryPair.t2
                when (findType(entry.value)) {
                    null -> {
                        Flux.empty()
                    }

                    NodeType.ARRAY -> {
                        Flux.fromIterable(entry.value)
                            .index()
                            .flatMap { indexNodePair ->
                                var identifier = indexNodePair.t1.toString()
                                val element = indexNodePair.t2
                                if (ignoreCollectionOrder) {
                                    identifier = getAltIdentifier(element, altIdentifierFields) ?: identifier
                                }
                                val fqcnValue =
                                    getFqcnValue(isObjectNode, identifier, fqcn, entry).plus(".").plus(identifier)
                                if (element.isValueNode) {
                                    val updatedValue = if (eventType == EventType.CREATED) element.asText() else null
                                    val previousValue = if (eventType == EventType.DELETED) element.asText() else null
                                    Flux.just(
                                        Element(
                                            name = entry.key,
                                            updatedValue = updatedValue,
                                            previousValue = previousValue,
                                            metadata = ElementMetadata(
                                                fqdn = fqcnValue
                                            )
                                        )
                                    )
                                } else {
                                    toElement(element, eventType, fqcnValue, ignoreCollectionOrder, altIdentifierFields)
                                }
                            }
                    }

                    NodeType.OBJECT -> {
                        toElement(
                            entry.value,
                            eventType,
                            getFqcnValue(isObjectNode, null, fqcn, entry),
                            ignoreCollectionOrder,
                            altIdentifierFields
                        )
                    }

                    else -> {
                        Flux.just(
                            Element(
                                name = entry.key,
                                updatedValue = if (eventType == EventType.CREATED) entry.value.asText() else null,
                                previousValue = if (eventType == EventType.DELETED) entry.value.asText() else null,
                                metadata = ElementMetadata(
                                    fqdn = getFqcnValue(isObjectNode, index, fqcn, entry)
                                )
                            )
                        )
                    }
                }
            }
    }

    /**
     * Derives the fully qualified class name for a given element.
     */
    private fun getFqcnValue(
        isObjectNode: Boolean,
        index: String?,
        fqcn: String,
        entry: Map.Entry<String, JsonNode>
    ): String {
        return if (isObjectNode) {
            fqcn.plus(".").plus(entry.key)
        } else {
            fqcn.plus(".").plus(index).plus(".").plus(entry.key)
        }
    }

    /**
     * Converts a node to iterables if it's type was collection.
     */
    private fun getIterables(isObjectNode: Boolean, node: JsonNode): List<MutableMap.MutableEntry<String, JsonNode>> {
        return if (isObjectNode) {
            node.fields().asSequence().toList()
        } else {
            node.toList().flatMap { it.fields().asSequence().toList() }
        }
    }

    /**
     * Determines the type of the [JsonNode] and returns and equivalent [NodeType]
     */
    private fun findType(node: JsonNode): NodeType? {
        return when {
            node.isValueNode -> NodeType.TEXT
            node.nodeType == JsonNodeType.ARRAY -> NodeType.ARRAY
            node.nodeType == JsonNodeType.OBJECT -> NodeType.OBJECT
            else -> null
        }
    }

    /**
     * Creates a string identifier to use instead of index when building fqcn
     */
    private fun getAltIdentifier(element: JsonNode, altIdentifierFields: List<String>): String? {
        var altIdentifier = ""
        if (element.isObject) element.fields()
            .forEach { (key, value) -> if (altIdentifierFields.contains(key)) altIdentifier += value.asText() }
        if (element.isValueNode) altIdentifier = element.asText().filterNot { it.isWhitespace() }
        return altIdentifier.ifEmpty { null }
    }
}
