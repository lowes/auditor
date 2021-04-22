package com.lowes.auditor.client.api

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.LogProvider
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.infrastructure.event.config.AuditEventModule
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.client.infrastructure.frameworks.config.FrameworkModule
import com.lowes.auditor.client.library.config.AuditorModule
import com.lowes.auditor.client.usecase.ExclusionFilter
import com.lowes.auditor.client.usecase.InclusionFilter
import com.lowes.auditor.core.entities.domain.EventSourceType
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import reactor.util.context.ContextView

interface Auditor {

    fun audit(oldObject: Any?, newObject: Any?)

    fun audit(oldObject: Any?, newObject: Any?, context: ContextView?)

    fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?)

    fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?, context: ContextView?)

    companion object {

        @JvmStatic
        fun getInstance(
            producerConfig: AuditEventProducerConfig,
            auditorEventConfig: AuditorEventConfig,
        ): Auditor {
            return getDefaultInstance(
                eventPublisher = getDefaultEventPublisher(producerConfig),
                logProvider = getDefaultLogProvider(),
                auditorEventConfig = auditorEventConfig
            )
        }

        @JvmStatic
        fun getInstance(
            producerConfig: AuditEventProducerConfig,
            auditorEventConfig: AuditorEventConfig,
            elementFilters: List<AuditEventElementFilter>,
        ): Auditor {
            return getDefaultInstance(
                eventPublisher = getDefaultEventPublisher(producerConfig),
                logProvider = getDefaultLogProvider(),
                auditorEventConfig = auditorEventConfig,
                elementFilters = elementFilters
            )
        }

        @JvmStatic
        fun getInstance(
            producerConfig: AuditEventProducerConfig,
            auditorEventConfig: AuditorEventConfig,
            elementFilters: List<AuditEventElementFilter>,
            auditorServiceScheduler: Scheduler,
        ): Auditor {
            return getDefaultInstance(
                eventPublisher = getDefaultEventPublisher(producerConfig),
                logProvider = getDefaultLogProvider(),
                auditorEventConfig = auditorEventConfig,
                elementFilters = elementFilters,
                auditorServiceScheduler = auditorServiceScheduler
            )
        }

        @JvmStatic
        fun getInstance(
            eventPublisher: EventPublisher,
            auditorEventConfig: AuditorEventConfig,
            elementFilters: List<AuditEventElementFilter>,
            auditorServiceScheduler: Scheduler,
            logProvider: LogProvider
        ): Auditor {
            return getDefaultInstance(
                eventPublisher = eventPublisher,
                logProvider = logProvider,
                auditorEventConfig = auditorEventConfig,
                elementFilters = elementFilters,
                auditorServiceScheduler = auditorServiceScheduler
            )
        }

        private fun getDefaultInstance(
            eventPublisher: EventPublisher,
            logProvider: LogProvider,
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
            val auditorModule = AuditorModule(auditorEventConfig, mergedElementFilters, eventPublisher, logProvider)
            return auditor(auditorModule, auditorServiceScheduler)
        }

        private fun auditor(auditorModule: AuditorModule, scheduler: Scheduler): Auditor {
            return AuditorService(auditorModule.auditEventGeneratorService, scheduler)
        }

        private fun getDefaultEventPublisher(producerConfig: AuditEventProducerConfig): EventPublisher {
            return AuditEventModule(producerConfig).auditEventProducerService
        }

        private fun getDefaultLogProvider(): LogProvider {
            return FrameworkModule.defaultLogProvider
        }
    }
}
