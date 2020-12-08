package com.lowes.auditor.client.library.config

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "auditor")
internal data class AuditorConfig(
    val config: AuditorEventConfig = AuditorEventConfig(
        applicationName = "NOT_CONFIGURED",
        eventSource = EventSource.SYSTEM,
    )
)
