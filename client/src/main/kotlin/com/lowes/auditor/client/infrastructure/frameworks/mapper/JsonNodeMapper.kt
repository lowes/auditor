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
    ): Flux<Element> {
        val hasFields = node.fields().hasNext()

        // Special handling for top-level arrays
        if (!hasFields && node.isArray) {
            return Flux.fromIterable(node)
                .index()
                .flatMap { tuple ->
                    val index = tuple.t1
                    val arrayItem = tuple.t2
                    val elementFqcn = "$fqcn.$index"
                    createElementForNode(arrayItem, index.toString(), eventType, elementFqcn)
                }
        }

        return Flux.fromIterable(getIterables(hasFields, node))
            .index()
            .flatMap { tuple ->
                val index = tuple.t1
                val entry = tuple.t2
                val elementFqcn = getFqcnValue(hasFields, index, fqcn, entry)
                val nodeType = findType(entry.value) ?: return@flatMap Flux.empty<Element>()

                when (nodeType) {
                    NodeType.ARRAY -> {
                        Flux.fromIterable(entry.value)
                            .index()
                            .flatMap { arrayTuple ->
                                val arrayIdx = arrayTuple.t1
                                val arrayItem = arrayTuple.t2
                                val arrayElementFqcn = "$elementFqcn.$arrayIdx"
                                createElementForNode(arrayItem, entry.key, eventType, arrayElementFqcn)
                            }
                    }
                    NodeType.OBJECT -> toElement(entry.value, eventType, elementFqcn)
                    else -> createElementForNode(entry.value, entry.key, eventType, elementFqcn, nodeType)
                }
            }
    }

    /**
     * Creates an Element for a node with the given parameters.
     */
    private fun createElementForNode(
        node: JsonNode,
        name: String,
        eventType: EventType,
        fqdn: String,
        nodeType: NodeType? = findType(node),
    ): Flux<Element> {
        if (nodeType == null) return Flux.empty()
        val updatedValue = if (eventType == EventType.CREATED) getValue(nodeType, node) else null
        val previousValue = if (eventType == EventType.DELETED) getValue(nodeType, node) else null

        return if (nodeType == NodeType.ARRAY || nodeType == NodeType.OBJECT) {
            toElement(node, eventType, fqdn)
        } else {
            Flux.just(Element(name, updatedValue, previousValue, ElementMetadata(fqdn = fqdn)))
        }
    }

    /**
     * Derives the fully qualified class name for a given element.
     */
    private fun getFqcnValue(
        hasFields: Boolean,
        index: Long,
        fqcn: String,
        entry: Map.Entry<String, JsonNode>,
    ): String {
        return if (hasFields) {
            fqcn.plus(".").plus(entry.key)
        } else {
            fqcn.plus(".").plus(index).plus(".").plus(entry.key)
        }
    }

    /**
     * Converts a node to iterables if it's type was collection.
     */
    private fun getIterables(
        hasFields: Boolean,
        node: JsonNode,
    ): List<MutableMap.MutableEntry<String, JsonNode>> {
        return if (hasFields) {
            node.fields().asSequence().toList()
        } else {
            node.toList().flatMap { it.fields().asSequence().toList() }
        }
    }

    /**
     * Gets the value of the leaf node
     */
    private fun getValue(
        nodeType: NodeType,
        node: JsonNode,
    ): String? {
        return when (nodeType) {
            NodeType.TEXT -> node.asText()
            else -> null
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
}
