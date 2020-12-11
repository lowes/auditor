package com.lowes.auditor.client.api

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.entities.domain.EventSourceType
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.infrastructure.event.config.AuditEventModule
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.client.library.config.AuditorModule
import com.lowes.auditor.client.usecase.ExclusionFilter
import com.lowes.auditor.client.usecase.InclusionFilter

interface Auditor {

    fun audit(oldObject: Any?, newObject: Any?)

    fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?)

    companion object {
        fun getInstance(
            producerConfig: AuditEventProducerConfig = AuditEventProducerConfig(),
            auditorEventConfig: AuditorEventConfig = AuditorEventConfig(
                applicationName = "NOT_CONFIGURED",
                eventSource = EventSourceConfig(type = EventSourceType.SYSTEM)
            ),
            elementFilters: List<AuditEventElementFilter> = emptyList()
        ): Auditor {
            val mergedElementFilters = setOf(
                InclusionFilter(),
                ExclusionFilter()
            ).plus(elementFilters).toList()
            val auditEventModule = AuditEventModule(producerConfig)
            val auditorModule = AuditorModule(auditorEventConfig, mergedElementFilters, auditEventModule.auditEventProducerService)
            return auditor(auditorModule)
        }

        private fun auditor(auditorModule: AuditorModule): Auditor {
            return AuditorService(auditorModule.auditEventGeneratorService)
        }
    }
}
