package com.lowes.auditor.client.library.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.core.entities.domain.AuditEvent
import com.lowes.auditor.core.entities.domain.EventSource
import com.lowes.auditor.core.entities.domain.EventSourceMetadata
import reactor.core.publisher.Flux
import java.lang.StringBuilder
import java.util.regex.Matcher
import java.util.regex.Pattern

class AuditEventDecoratorService(
    private val auditorObjectMapper: ObjectMapper,
    // supports pattern of form - ${JsonNodeHere}
    private val compiledPattern: Pattern = Pattern.compile("(\\\$\\{[\\w.]+\\})+")
) {

    fun decorate(events: Flux<AuditEvent>, auditorEventConfig: AuditorEventConfig, newObject: Any?): Flux<AuditEvent> {
        return events.map {
            val rootNode = auditorObjectMapper.valueToTree<JsonNode>(newObject)
            val source = it.source
            val subtype = it.subType
            val metadata = it.metadata
            it.copy(
                source = fetchSource(rootNode, source),
                subType = fetchNodeValue(rootNode, subtype),
                metadata = fetchMetadata(rootNode, metadata)
            )
        }
    }

    private fun fetchSource(rootNode: JsonNode, source: EventSource): EventSource {
        return source.copy(
            metadata = EventSourceMetadata(
                id = fetchNodeValue(rootNode, source.metadata?.id),
                email = fetchNodeValue(rootNode, source.metadata?.email),
                name = fetchNodeValue(rootNode, source.metadata?.name)
            )
        )
    }

    private fun fetchMetadata(rootNode: JsonNode, metadata: Map<String, String>?): Map<String, String>? {
        return metadata?.map { metadata ->
            val key = metadata.key
            val value = metadata.value
            key to fetchNodeValue(rootNode, value).orEmpty()
        }?.filter {
            it.second.isNotBlank()
        }?.toMap()
    }

    private fun fetchNodeValue(rootNode: JsonNode, pathValue: String?): String? {
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
