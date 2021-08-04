package com.lowes.auditor.client.entities.domain

import com.lowes.auditor.core.entities.domain.EventSource
import com.lowes.auditor.core.entities.domain.EventSourceMetadata
import com.lowes.auditor.core.entities.domain.EventSourceType
import com.lowes.auditor.core.entities.domain.EventType

data class AuditorEventConfig(
    var applicationName: String? = null,
    var eventSource: EventSourceConfig? = null,
    var eventSubType: String? = null,
    var metadata: Map<String, String>? = null,
    var filters: Filters? = null,
    var maxElements: Int? = null
)

data class Filters(
    var event: EventFilter? = null,
    var element: ElementFilter? = null,
    var logging: LoggingFilter? = null,
)

data class EventFilter(
    var enabled: Boolean = false,
    var type: List<EventType>? = null,
)

data class ElementFilter(
    var enabled: Boolean = false,
    var types: List<String>? = null,
    var options: ElementFilterOptions? = null,
)

data class LoggingFilter(
    var enabled: Boolean = false,
)

data class ElementFilterOptions(
    var includes: List<String>? = null,
    var excludes: List<String>? = null,
    var metaData: Map<String, String>? = null
)

data class EventSourceConfig(
    var type: EventSourceType? = null,
    var metadata: EventSourceMetadataConfig? = null,
) {
    fun toEventSource(): EventSource {
        return EventSource(
            type = this.type ?: EventSourceType.SYSTEM,
            metadata = this.metadata?.toEventSourceMetadata()
        )
    }
}

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
