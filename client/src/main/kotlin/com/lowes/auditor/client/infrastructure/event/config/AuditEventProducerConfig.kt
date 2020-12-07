package com.lowes.auditor.client.infrastructure.event.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "auditor.producer")
internal data class AuditEventProducerConfig constructor(
    var enabled: Boolean? = false,
    var bootstrapServers: String? = null,
    var topic: String? = null,
    var configs: Map<String, String>? = null
)
