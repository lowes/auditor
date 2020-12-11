package com.lowes.auditor.client.springboot

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.entities.domain.EventSourceType
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "auditor")
class AuditorAutoConfig {
    var config: AuditorEventConfig = AuditorEventConfig(
        applicationName = "NOT_CONFIGURED",
        eventSource = EventSourceConfig(
            type = EventSourceType.SYSTEM
        )
    )
    var producer: AuditEventProducerConfig = AuditEventProducerConfig()
}
