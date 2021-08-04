package com.lowes.auditor.client.springboot.library.config

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.core.entities.constants.AUDITOR
import com.lowes.auditor.core.entities.constants.NOT_CONFIGURED
import com.lowes.auditor.core.entities.domain.EventSourceType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = AUDITOR)
class AuditorAutoConfig {
    var config: AuditorEventConfig = AuditorEventConfig(
        applicationName = NOT_CONFIGURED,
        eventSource = EventSourceConfig(
            type = EventSourceType.SYSTEM
        ),
        maxElements = 500
    )
    var producer: AuditEventProducerConfig = AuditEventProducerConfig()
}
