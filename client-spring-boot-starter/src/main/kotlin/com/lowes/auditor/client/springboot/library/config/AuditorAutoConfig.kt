package com.lowes.auditor.client.springboot.library.config

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.core.entities.constants.AUDITOR
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Data class containing configurations for [AuditorEventConfig] and [AuditEventProducerConfig].
 * It is initialized implicitly by [AuditorAutoConfigureModule] by reading configurations present
 * in the application config file when auto configurations are enabled.
 *
 * @property config instance of [AuditorEventConfig]
 * @property producer instance of [AuditEventProducerConfig]
 * @constructor creates an instance of AuditorAutoConfig
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = AUDITOR)
data class AuditorAutoConfig(
    val config: AuditorEventConfig = AuditorEventConfig.getDefaultInstance(),
    val producer: AuditEventProducerConfig = AuditEventProducerConfig()
)
