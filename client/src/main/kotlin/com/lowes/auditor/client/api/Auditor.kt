package com.lowes.auditor.client.api

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.infrastructure.event.config.AuditEventModule
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.client.library.config.AuditorModule
import com.lowes.auditor.client.usecase.ExclusionFilter
import com.lowes.auditor.client.usecase.InclusionFilter
import com.lowes.auditor.core.entities.domain.EventSourceType
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers

interface Auditor {

    fun audit(oldObject: Any?, newObject: Any?)

    fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?)

    companion object {

        fun getInstance(): Auditor {
            return getDefaultInstance()
        }

        fun getInstance(producerConfig: AuditEventProducerConfig): Auditor {
            return getDefaultInstance(
                producerConfig = producerConfig
            )
        }

        fun getInstance(auditorEventConfig: AuditorEventConfig): Auditor {
            return getDefaultInstance(
                auditorEventConfig = auditorEventConfig
            )
        }

        fun getInstance(
            producerConfig: AuditEventProducerConfig,
            auditorEventConfig: AuditorEventConfig,
        ): Auditor {
            return getDefaultInstance(
                producerConfig = producerConfig,
                auditorEventConfig = auditorEventConfig
            )
        }

        fun getInstance(
            producerConfig: AuditEventProducerConfig,
            auditorEventConfig: AuditorEventConfig,
            elementFilters: List<AuditEventElementFilter>,
        ): Auditor {
            return getDefaultInstance(
                producerConfig = producerConfig,
                auditorEventConfig = auditorEventConfig,
                elementFilters = elementFilters
            )
        }

        fun getInstance(
            producerConfig: AuditEventProducerConfig,
            auditorEventConfig: AuditorEventConfig,
            elementFilters: List<AuditEventElementFilter>,
            auditorServiceScheduler: Scheduler,
        ): Auditor {
            return getDefaultInstance(
                producerConfig = producerConfig,
                auditorEventConfig = auditorEventConfig,
                elementFilters = elementFilters,
                auditorServiceScheduler = auditorServiceScheduler
            )
        }

        private fun getDefaultInstance(
            producerConfig: AuditEventProducerConfig = AuditEventProducerConfig(),
            auditorEventConfig: AuditorEventConfig = AuditorEventConfig(
                applicationName = "NOT_CONFIGURED",
                eventSource = EventSourceConfig(type = EventSourceType.SYSTEM)
            ),
            elementFilters: List<AuditEventElementFilter> = emptyList(),
            auditorServiceScheduler: Scheduler = Schedulers.newParallel("auditorServiceScheduler")
        ): Auditor {
            val mergedElementFilters = setOf(
                InclusionFilter(),
                ExclusionFilter()
            ).plus(elementFilters).toList()
            val auditEventModule = AuditEventModule(producerConfig)
            val auditorModule = AuditorModule(auditorEventConfig, mergedElementFilters, auditEventModule.auditEventProducerService)
            return auditor(auditorModule, auditorServiceScheduler)
        }

        private fun auditor(auditorModule: AuditorModule, scheduler: Scheduler): Auditor {
            return AuditorService(auditorModule.auditEventGeneratorService, scheduler)
        }
    }
}
