package com.lowes.auditor.client.entities.domain

import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
data class AuditorEventConfig(
    var applicationName: String? = null,
    var eventSource: EventSourceConfig? = null,
    var eventSubType: String? = null,
    var metadata: Map<String, String>? = null,
    var filters: Filters? = null,
)

@Configuration(proxyBeanMethods = false)
data class Filters(
    var event: EventFilter? = null,
    var element: ElementFilter? = null,
    var logging: LoggingFilter? = null,
)

@Configuration(proxyBeanMethods = false)
data class EventFilter(
    var enabled: Boolean = false,
    var type: List<EventType>? = null,
)

@Configuration(proxyBeanMethods = false)
data class ElementFilter(
    var enabled: Boolean = false,
    var types: List<String>? = null,
    var options: ElementFilterOptions? = null,
)

@Configuration(proxyBeanMethods = false)
data class LoggingFilter(
    var enabled: Boolean = false,
)

@Configuration(proxyBeanMethods = false)
data class ElementFilterOptions(
    var includes: List<String>? = null,
    var excludes: List<String>? = null,
    var metaData: Map<String, String>? = null
)

@Configuration(proxyBeanMethods = false)
data class EventSourceConfig(
    var type: EventSourceType = EventSourceType.SYSTEM,
    var metadata: EventSourceMetadataConfig? = null,
) {
    fun toEventSource(): EventSource {
        return EventSource(
            type = this.type,
            metadata = this.metadata?.toEventSourceMetadata()
        )
    }
}

@Configuration(proxyBeanMethods = false)
data class EventSourceMetadataConfig(
    var id: String? = null,
    var email: String? = null,
    var name: String? = null,
) {
    fun toEventSourceMetadata(): EventSourceMetadata {
        return EventSourceMetadata(
            id = this.id,
            email = this.email,
            name = this.name,
        )
    }
}
