package com.lowes.auditor.client.library.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.entities.domain.AuditEvent
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.entities.util.orDefault
import reactor.core.publisher.Flux
import java.lang.StringBuilder
import java.util.regex.Matcher
import java.util.regex.Pattern

internal class AuditEventDecoratorService(
    private val auditorObjectMapper: ObjectMapper,
    // supports pattern of form - ${JsonNodeHere}
    private val compiledPattern: Pattern = Pattern.compile("(\\\$\\{[\\w.]+\\})+")
) {

    fun decorate(events: Flux<AuditEvent>, auditorEventConfig: AuditorEventConfig, newObject: Any?): Flux<AuditEvent> {
        return events.map {
            val rootNode = auditorObjectMapper.valueToTree<JsonNode>(newObject)
            it.copy(
                applicationName = auditorEventConfig.applicationName.orEmpty(),
                source = auditorEventConfig.eventSource.orDefault(EventSourceConfig()).toEventSource(),
                subType = fetchNodeValue(rootNode, auditorEventConfig.eventSubType),
                metadata = fetchMetadata(rootNode, auditorEventConfig)
            )
        }
    }

    private fun fetchMetadata(rootNode: JsonNode, auditorEventConfig: AuditorEventConfig): Map<String, String>? {
        return auditorEventConfig.metadata?.map { metadata ->
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
