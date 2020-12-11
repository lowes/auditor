package com.lowes.auditor.client.springboot

import com.lowes.auditor.client.api.Auditor
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuditorAutoConfig::class)
internal class AuditorAutoConfigureModule(
    private val auditorAutoConfig: AuditorAutoConfig
) {

    @Bean
    fun auditor(elementFilters: List<AuditEventElementFilter>): Auditor {
        return Auditor.getInstance(
            auditorAutoConfig.producer,
            auditorAutoConfig.config,
            elementFilters
        )
    }
}
