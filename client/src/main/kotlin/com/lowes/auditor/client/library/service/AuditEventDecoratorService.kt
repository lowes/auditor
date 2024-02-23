package com.lowes.auditor.client.library.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.core.entities.domain.AuditEvent
import com.lowes.auditor.core.entities.domain.EventSource
import com.lowes.auditor.core.entities.domain.EventSourceMetadata
import reactor.core.publisher.Flux
import java.lang.StringBuilder
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Provides functionalities to decorate incoming flux of [AuditEvent] with additional metadata
 * @property auditorObjectMapper instance of [ObjectMapper]
 * @property compiledPattern instance of [Pattern]
 */
class AuditEventDecoratorService(
    private val auditorObjectMapper: ObjectMapper,
    // supports pattern of form - ${JsonNodeHere}
    private val compiledPattern: Pattern = Pattern.compile("(\\\$\\{[\\w.]+\\})+"),
) {
    /**
     * Decorates incoming flux of [AuditEvent] with additional metadata.
     * These metadata is derived from [newObject] by converting to a [JsonNode]
     * which is then used to parse out the additional metadata
     * @param events flux of [AuditEvent]
     * @param newObject instance of [Any]
     * @return flux of decorated [AuditEvent]
     */
    fun decorate(
        events: Flux<AuditEvent>,
        newObject: Any?,
    ): Flux<AuditEvent> {
        return events.map {
            val rootNode = auditorObjectMapper.valueToTree<JsonNode>(newObject)
            val source = it.source
            val subtype = it.subType
            val metadata = it.metadata
            it.copy(
                source = fetchSource(rootNode, source),
                subType = fetchNodeValue(rootNode, subtype),
                metadata = fetchMetadata(rootNode, metadata),
            )
        }
    }

    /**
     * Derives audit source information from the new objects' root [JsonNode]
     * and overrides the existing source metadata present in [EventSource].
     */
    private fun fetchSource(
        rootNode: JsonNode,
        source: EventSource,
    ): EventSource {
        return source.copy(
            metadata =
                EventSourceMetadata(
                    id = fetchNodeValue(rootNode, source.metadata?.id),
                    email = fetchNodeValue(rootNode, source.metadata?.email),
                    name = fetchNodeValue(rootNode, source.metadata?.name),
                ),
        )
    }

    /**
     * Fetches metadata information from the new objects' root [JsonNode] and returns the same. If its empty/blank
     * existing metadata information from [metadataParam] is returned
     */
    private fun fetchMetadata(
        rootNode: JsonNode,
        metadataParam: Map<String, String>?,
    ): Map<String, String>? {
        return metadataParam?.map { metadata ->
            val key = metadata.key
            val value = metadata.value
            key to fetchNodeValue(rootNode, value).orEmpty()
        }?.filter {
            it.second.isNotBlank()
        }?.toMap()
    }

    /**
     * Fetches node vale from [JsonNode] by looking up [pathValue] in the node.
     */
    private fun fetchNodeValue(
        rootNode: JsonNode,
        pathValue: String?,
    ): String? {
        return pathValue?.let {
            val matcher: Matcher = compiledPattern.matcher(pathValue)
            val sb = StringBuilder(pathValue.length)
            while (matcher.find()) {
                val jsonField = "/".plus(matcher.group(1).drop(2).dropLast(1).replace(".", "/"))
                val text = rootNode.at(jsonField).asText()
                text?.let { matcher.appendReplacement(sb, Matcher.quoteReplacement(text)) }
            }
            matcher.appendTail(sb).toString()
        }
    }
}
