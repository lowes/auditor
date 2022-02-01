package com.lowes.auditor.client.entities.domain

import com.lowes.auditor.client.entities.util.FIVE_HUNDRED
import com.lowes.auditor.client.entities.util.TEN
import com.lowes.auditor.client.entities.util.THIRTY
import com.lowes.auditor.core.entities.constants.NOT_CONFIGURED
import com.lowes.auditor.core.entities.domain.EventSource
import com.lowes.auditor.core.entities.domain.EventSourceMetadata
import com.lowes.auditor.core.entities.domain.EventSourceType
import com.lowes.auditor.core.entities.domain.EventType
import java.time.Duration

/**
 * Data class containing configurations for auditor. Property values are kept as var for easy java interoperability.
 * @property applicationName name of the application that will be sent as part of audit
 * @property eventSource instance of [EventSourceConfig]
 * @property eventSubType subtype of the audit event that will be sent as part of audit
 * @property metadata additional metadata to be associated to each audit
 * @property filters instance of [Filters] that provides different filters to run during the audit generation
 * @property maxElements maximum number of elements to be audited.
 * @property retry instance of [RetryPublisherConfig] controlling number of retries in case of errors.
 */
data class AuditorEventConfig(
    var applicationName: String? = null,
    var eventSource: EventSourceConfig? = null,
    var eventSubType: String? = null,
    var metadata: Map<String, String>? = null,
    var filters: Filters? = null,
    var maxElements: Int? = null,
    var retry: RetryPublisherConfig? = null,
) {
    companion object {
        /**
         * Creates instance of [AuditorEventConfig] with appropriate defaults
         * @return instance of [AuditorEventConfig]
         */
        fun getDefaultInstance(): AuditorEventConfig {
            return AuditorEventConfig(
                applicationName = NOT_CONFIGURED,
                eventSource = EventSourceConfig(type = EventSourceType.SYSTEM),
                maxElements = FIVE_HUNDRED,
                retry = RetryPublisherConfig(
                    enabled = true,
                    count = TEN.toLong(),
                    delay = Duration.ofSeconds(THIRTY.toLong())
                )
            )
        }
    }
}

/**
 * Data class containing filter details. These filters are used during audit generation process to include/exclude/log
 * audit elements.
 * @property event instance of [EventFilter] which filters audit events based on event type
 * @property element instance of [ElementFilter] which filters elements based on Element type
 * @property logging instance of [LoggingFilter] which logs audit events
 */
data class Filters(
    val event: EventFilter? = null,
    val element: ElementFilter? = null,
    val logging: LoggingFilter? = null,
)

/**
 * Data class containing event filters details. These filters events based on [EventType]
 * @property enabled [Boolean] flag to enable/disable the feature
 * @property type list of [EventType]
 */
data class EventFilter(
    val enabled: Boolean? = null,
    val type: List<EventType>? = null,
)

/**
 * Data class containing element filters details. These filters elements based on [EventType]
 * @property enabled [Boolean] flag to enable/disable the feature
 * @property types list of element filters like "InclusionFilter" or "ExclusionFilter"
 * @property options instance of [ElementFilterOptions]
 */
data class ElementFilter(
    val enabled: Boolean? = null,
    val types: List<String>? = null,
    val options: ElementFilterOptions? = null,
)

/**
 * Data class containing logging filter details
 * @property enabled [Boolean] flag to enable/disable the logging feature
 */
data class LoggingFilter(
    val enabled: Boolean? = null,
)

/**
 * Data class containing configurations for Element filters.
 * @property includes list of element which will be used when an "InclusionFilter" is used.
 * If an audit element name matches any of [includes], it will be included
 * @property excludes list of element which will be used when an "ExclusionFilter" is used.
 * If an audit element name matches any of [excludes], it will be excluded
 * @property metaData additional metadata that can be used to decorate audited elements
 */
data class ElementFilterOptions(
    val includes: List<String>? = null,
    val excludes: List<String>? = null,
    val metaData: Map<String, String>? = null
)

/**
 * Data class containing configurations for event source
 * @property type instance of [EventSourceType]
 * @property metadata instance of [EventSourceMetadataConfig]
 */
data class EventSourceConfig(
    val type: EventSourceType? = null,
    val metadata: EventSourceMetadataConfig? = null,
) {
    fun toEventSource(): EventSource {
        return EventSource(
            type = this.type ?: EventSourceType.SYSTEM,
            metadata = this.metadata?.toEventSourceMetadata()
        )
    }
}

/**
 * Data class containing metadata configurations for an event source
 * @property id identifier for the event source
 * @property email email for the event source
 * @property name name for the event source
 */
data class EventSourceMetadataConfig(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
) {
    fun toEventSourceMetadata(): EventSourceMetadata {
        return EventSourceMetadata(
            id = this.id,
            email = this.email,
            name = this.name,
        )
    }
}

/**
 * Data class configurations for retry publisher
 * @property enabled [Boolean] flag to enable/disable the logging feature
 * @property count number of times a retry will be attempted for failed event publish
 * @property delay instance of [Duration] signifying delay between consecutive retry attempts.
 */
data class RetryPublisherConfig(
    val enabled: Boolean? = null,
    val count: Long? = null,
    val delay: Duration? = null
)
