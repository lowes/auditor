package com.lowes.auditor.client.entities.domain

data class AuditorEventConfig(
    val applicationName: String? = null,
    val eventSource: EventSource? = null,
    val eventSubType: String? = null,
    val eventSourceMetadata: EventSourceMetadata? = null,
    val metadata: Map<String, String>? = null,
) {
    fun copyIfNull(eventConfig: AuditorEventConfig): AuditorEventConfig {
        return this.copy(
            applicationName = this.applicationName ?: eventConfig.applicationName,
            eventSource = this.eventSource ?: eventConfig.eventSource,
            eventSubType = this.eventSubType ?: eventConfig.eventSubType,
            eventSourceMetadata = this.eventSourceMetadata ?: eventConfig.eventSourceMetadata,
            metadata = this.metadata ?: eventConfig.metadata,
        )
    }
}
