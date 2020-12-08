package com.lowes.auditor.client.library.config

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSource
import com.lowes.auditor.client.entities.domain.EventSourceMetadata
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "auditor")
internal data class AuditorConfig(
    var config: Config = Config()
) {
    fun toAuditEventConfig(): AuditorEventConfig {
        val config = this.config
        return AuditorEventConfig(
            applicationName = config.applicationName,
            eventSource = config.eventSource,
            eventSubType = config.eventSubType,
            eventSourceMetadata = config.eventSourceMetadata,
            metadata = config.metadata
        )
    }
}

@Configuration(proxyBeanMethods = false)
internal data class Config(
    var applicationName: String = "NOT_CONFIGURED",
    var eventSource: EventSource = EventSource.SYSTEM,
    var eventSubType: String? = null,
    var eventSourceMetadata: EventSourceMetadata? = null,
    var metadata: Map<String, String>? = null,
)
