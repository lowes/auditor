package com.lowes.auditor.client.infrastructure.event.config

data class AuditEventProducerConfig(
    var enabled: Boolean? = false,
    var bootstrapServers: String? = null,
    var topic: String? = null,
    var configs: Map<String, String>? = null
)
