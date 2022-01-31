package com.lowes.auditor.client.library.config

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.LogProvider
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventElementFilter
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventFilter
import com.lowes.auditor.client.infrastructure.frameworks.config.FrameworkModule
import com.lowes.auditor.client.library.service.AuditEventDecoratorService
import com.lowes.auditor.client.library.service.AuditEventFilterService
import com.lowes.auditor.client.library.service.AuditEventGeneratorService
import com.lowes.auditor.client.usecase.ElementAggregatorUseCase
import com.lowes.auditor.client.usecase.ElementFilterUseCase
import com.lowes.auditor.client.usecase.EventFilterUseCase
import com.lowes.auditor.client.usecase.EventLogUseCase
import com.lowes.auditor.client.usecase.LoggingFilterUseCase
import com.lowes.auditor.core.entities.util.JsonObject

/**
 * Initializes and configures Auditor module
 * @property auditorEventConfig instance of [AuditorEventConfig]
 * @property elementFilters list of [AuditEventElementFilter]
 * @property eventPublisher instance of [EventPublisher]
 * @property logProvider instance of [logProvider]
 */
class AuditorModule(
    private val auditorEventConfig: AuditorEventConfig,
    private val elementFilters: List<AuditEventElementFilter>,
    private val eventPublisher: EventPublisher,
    private val logProvider: LogProvider
) {
    /**
     * Provides singleton and lazily initialized instance of [AuditEventGeneratorService]
     */
    val auditEventGeneratorService: AuditEventGeneratorService by lazy {
        AuditEventGeneratorService(
            elementAggregatorUseCase,
            auditorEventConfig,
            eventPublisher,
            auditEventFilterService,
            auditEventDecoratorService,
            eventLogUseCase
        )
    }

    /**
     * Provides singleton and lazily initialized instance of [ElementAggregatorUseCase]
     */
    private val elementAggregatorUseCase: ElementAggregatorUseCase by lazy {
        ElementAggregatorUseCase(FrameworkModule.getObjectDiffChecker(auditorEventConfig))
    }

    /**
     * Provides singleton and lazily initialized instance of [ElementFilterUseCase] of type [AuditEventElementFilter]
     */
    private val elementFilterUseCase: AuditEventElementFilter by lazy {
        ElementFilterUseCase(elementFilters)
    }

    /**
     * Provides singleton and lazily initialized instance of [EventFilterUseCase] of type [AuditEventFilter]
     */
    private val eventFilterUseCase: AuditEventFilter by lazy {
        EventFilterUseCase()
    }

    /**
     * Provides singleton and lazily initialized instance of [LoggingFilterUseCase] of type [AuditEventFilter]
     */
    private val loggingFilterUseCase: AuditEventFilter by lazy {
        LoggingFilterUseCase(logProvider)
    }

    /**
     * Provides singleton and lazily initialized instance of [AuditEventFilterService]
     */
    private val auditEventFilterService: AuditEventFilterService by lazy {
        AuditEventFilterService(eventFilterUseCase, elementFilterUseCase, loggingFilterUseCase)
    }

    /**
     * Provides singleton and lazily initialized instance of [AuditEventDecoratorService]
     */
    private val auditEventDecoratorService: AuditEventDecoratorService by lazy {
        AuditEventDecoratorService(JsonObject.objectMapper)
    }

    /**
     * Provides singleton and lazily initialized instance of [EventLogUseCase]
     */
    private val eventLogUseCase: EventLogUseCase by lazy {
        EventLogUseCase(FrameworkModule.objectLogGenerator)
    }
}
