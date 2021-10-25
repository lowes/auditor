package com.lowes.auditor.client.springboot.library.config

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.core.entities.constants.AUDITOR
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = AUDITOR)
class AuditorAutoConfig {
    var config: AuditorEventConfig = AuditorEventConfig.getDefaultInstance()
    var producer: AuditEventProducerConfig = AuditEventProducerConfig()
}
