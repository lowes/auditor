package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.Filters
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.LogProvider
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventFilter
import com.lowes.auditor.core.entities.domain.AuditEvent
import org.slf4j.LoggerFactory
import reactor.util.context.ContextView

/**
 * Provides implementation of [AuditEventFilter] for logging audit even object.
 * @property logProvider instance of underlying [LogProvider]
 */
class LoggingFilterUseCase(
    private val logProvider: LogProvider
) : AuditEventFilter {
    private val logger = LoggerFactory.getLogger(LoggingFilterUseCase::class.java)

    /**
     * Logs audit events based on [filters] and adding relevant [context] metadata
     * @see AuditEventFilter.filter
     */
    override fun filter(context: ContextView, event: AuditEvent, filters: Filters?): Boolean {
        val loggingFilter = filters?.logging
        return if (loggingFilter?.enabled == true) {
            logProvider.log(context, logger, event)
            true
        } else {
            true
        }
    }
}
