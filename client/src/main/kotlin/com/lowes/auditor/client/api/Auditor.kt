package com.lowes.auditor.client.api

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.LogProvider
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.infrastructure.event.config.AuditEventModule
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.client.infrastructure.frameworks.config.FrameworkModule
import com.lowes.auditor.client.library.config.AuditorModule
import com.lowes.auditor.client.usecase.ExclusionFilter
import com.lowes.auditor.client.usecase.InclusionFilter
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import reactor.util.context.ContextView

/**
 * Auditor api interface for exposing library functions like audit and log
 */
interface Auditor {

    /**
     * Performs an audit by comparing old and new objects
     * @param oldObject previous instance of the object to be compared against
     * @param newObject latest instance of the object that needs to be audited
     */
    fun audit(oldObject: Any?, newObject: Any?)

    /**
     * Performs an audit by comparing old and new objects
     * @param oldObject previous instance of the object to be compared against
     * @param newObject latest instance of the object that needs to be audited
     * @param context an instance of [ContextView] containing context relevant metadata
     */
    fun audit(oldObject: Any?, newObject: Any?, context: ContextView?)

    /**
     * Performs an audit by comparing old and new objects
     * @param oldObject previous instance of the object to be compared against
     * @param newObject latest instance of the object that needs to be audited
     * @param auditorEventConfig an instance of [AuditorEventConfig] containing audit related configurations
     */
    fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?)

    /**
     * Performs an audit by comparing old and new objects
     * @param oldObject previous instance of the object to be compared against
     * @param newObject latest instance of the object that needs to be audited
     * @param auditorEventConfig an instance of [AuditorEventConfig] containing audit related configurations
     * @param context an instance of [ContextView] containing context relevant metadata
     */
    fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?, context: ContextView?)

    /**
     * Generates an audit log containing an entity
     * @param entity entity of type [Any] that needs to ve logged
     */
    fun log(entity: Any)

    /**
     * Generates an audit log containing an entity
     * @param entity entity of type [Any] that needs to ve logged
     * @param context an instance of [ContextView] containing context relevant metadata
     */
    fun log(entity: Any, context: ContextView?)

    /**
     * Generates an audit log containing an entity
     * @param entity entity of type [Any] that needs to ve logged
     * @param auditorEventConfig an instance of [AuditorEventConfig] containing audit related configurations
     */
    fun log(entity: Any, auditorEventConfig: AuditorEventConfig?)

    /**
     * Generates an audit log containing an entity
     * @param entity entity of type [Any] that needs to ve logged
     * @param auditorEventConfig an instance of [AuditorEventConfig] containing audit related configurations
     * @param context an instance of [ContextView] containing context relevant metadata
     */
    fun log(entity: Any, auditorEventConfig: AuditorEventConfig?, context: ContextView?)

    companion object {

        /**
         * Creates an instance of [Auditor]
         * @param producerConfig instance of [AuditEventProducerConfig] containing producer configs
         * @param auditorEventConfig instance of [AuditorEventConfig] containing auditor configs
         * @return an instance of [Auditor]
         */
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

        /**
         * Creates an instance of [Auditor]
         * @param producerConfig instance of [AuditEventProducerConfig] containing producer configs
         * @param auditorEventConfig instance of [AuditorEventConfig] containing auditor configs
         * @param elementFilters list of [AuditEventElementFilter] containing all audit element filters
         * @return an instance of [Auditor]
         */
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

        /**
         * Creates an instance of [Auditor]
         * @param producerConfig instance of [AuditEventProducerConfig] containing producer configs
         * @param auditorEventConfig instance of [AuditorEventConfig] containing auditor configs
         * @param elementFilters list of [AuditEventElementFilter] containing all audit element filters
         * @param auditorServiceScheduler instance of of [Scheduler] which manages audit operations
         * @return an instance of [Auditor]
         */
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

        /**
         * Creates an instance of [Auditor]
         * @param eventPublisher instance of [EventPublisher] which is used pubish data to event stream
         * @param auditorEventConfig instance of [AuditorEventConfig] containing auditor configs
         * @param elementFilters list of [AuditEventElementFilter] containing all audit element filters
         * @param auditorServiceScheduler instance of of [Scheduler] which manages audit operations
         * @return an instance of [Auditor]
         */
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

        /**
         * Gets the default instance of auditor with appropriate
         * defaults for auditorEventConfig, elementFilters and auditorServiceScheduler
         */
        private fun getDefaultInstance(
            eventPublisher: EventPublisher,
            logProvider: LogProvider,
            auditorEventConfig: AuditorEventConfig = AuditorEventConfig.getDefaultInstance(),
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

        /**
         * Returns an instance of [Auditor]
         */
        private fun auditor(auditorModule: AuditorModule, scheduler: Scheduler): Auditor {
            return AuditorService(auditorModule.auditEventGeneratorService, scheduler)
        }

        /**
         * Returns default instance of [EventPublisher]
         */
        private fun getDefaultEventPublisher(producerConfig: AuditEventProducerConfig): EventPublisher {
            return AuditEventModule(producerConfig).auditEventProducerService
        }

        /**
         * Returns default instance of [LogProvider]
         */
        private fun getDefaultLogProvider(): LogProvider {
            return FrameworkModule.defaultLogProvider
        }
    }
}
