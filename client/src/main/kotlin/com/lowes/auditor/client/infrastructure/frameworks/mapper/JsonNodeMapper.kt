package com.lowes.auditor.client.infrastructure.frameworks.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.lowes.auditor.client.infrastructure.frameworks.model.NodeType
import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.ElementMetadata
import com.lowes.auditor.core.entities.domain.EventType
import reactor.core.publisher.Flux

internal object JsonNodeMapper {

    fun toElement(node: JsonNode, eventType: EventType, fqcn: String): Flux<Element> {
        val hasFields = node.fields().hasNext()
        val iterables = if (hasFields) {
            node.fields().asSequence().toList()
        } else {
            node.toList().flatMap { it.fields().asSequence().toList() }
        }
        return Flux.fromIterable(iterables)
            .index()
            .flatMap { indexEntryPair ->
                val index = indexEntryPair.t1
                val entry = indexEntryPair.t2
                findType(entry.value)
                    ?.let {
                        if (it == NodeType.ITERABLES) {
                            toElement(entry.value, eventType, getFqcnValue(hasFields, index, fqcn, entry))
                        } else {
                            val updatedValue = if (eventType == EventType.CREATED) {
                                getValue(it, entry.value)
                            } else null
                            val previousValue = if (eventType == EventType.DELETED) {
                                getValue(it, entry.value)
                            } else null
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

    private fun getFqcnValue(
        hasFields: Boolean,
        index: Long,
        fqcn: String,
        entry: Map.Entry<String, JsonNode>
    ): String {
        return if (hasFields) {
            fqcn.plus(".").plus(entry.key)
        } else {
            fqcn.plus(".").plus(index).plus(".").plus(entry.key)
        }
    }

    private fun getValue(nodeType: NodeType, node: JsonNode): String? {
        return when (nodeType) {
            NodeType.TEXT -> node.asText()
            else -> null
        }
    }

    private fun findType(node: JsonNode): NodeType? {
        return when {
            node.isValueNode -> NodeType.TEXT
            node.isContainerNode -> NodeType.ITERABLES
            else -> null
        }
    }
}
