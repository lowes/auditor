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
        val hasFields = node.fields().hasNext()
        return Flux.fromIterable(getIterables(hasFields, node))
            .index()
            .flatMap { indexEntryPair ->
                val index = indexEntryPair.t1.toString()
                val entry = indexEntryPair.t2
                findType(entry.value)
                    ?.let {
                        if (it == NodeType.ARRAY) {
                            Flux.fromIterable(entry.value)
                                .index()
                                .flatMap { t ->
                                    var identifier = t.t1.toString()
                                    val element = t.t2
                                    if (ignoreCollectionOrder) {
                                        identifier = getAltIdentifier(element, altIdentifierFields) ?: identifier
                                    }
                                    val fqcnValue = getFqcnValue(hasFields, identifier, fqcn, entry).plus(".").plus(identifier)
                                    if (t.t2.isValueNode) {
                                        val updatedValue = if (eventType == EventType.CREATED) {
                                            findType(t.t2)?.let { it1 -> getValue(it1, t.t2) }
                                        } else null
                                        val previousValue = if (eventType == EventType.DELETED) {
                                            findType(t.t2)?.let { it1 -> getValue(it1, t.t2) }
                                        } else null
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
                        } else if (it == NodeType.OBJECT) {
                            toElement(entry.value, eventType, getFqcnValue(hasFields, index, fqcn, entry), ignoreCollectionOrder, altIdentifierFields)
                        } else {
                            val updatedValue = if (eventType == EventType.CREATED) getValue(it, entry.value) else null
                            val previousValue = if (eventType == EventType.DELETED) getValue(it, entry.value) else null
                            Flux.just(
                                Element(
                                    name = entry.key,
                                    updatedValue = updatedValue,
                                    previousValue = previousValue,
                                    metadata = ElementMetadata(
                                        fqdn = getFqcnValue(hasFields, index, fqcn, entry)
                                    )
                                )
                            )
                        }
                    } ?: Flux.empty()
            }
    }

    /**
     * Derives the fully qualified class name for a given element.
     */
    private fun getFqcnValue(
        hasFields: Boolean,
        index: String?,
        fqcn: String,
        entry: Map.Entry<String, JsonNode>
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
