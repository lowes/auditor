package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.Filters
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.LogProvider
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventFilter
import com.lowes.auditor.core.entities.domain.AuditEvent
import org.slf4j.LoggerFactory
import reactor.util.context.ContextView

internal class LoggingFilterUseCase(
    private val logProvider: LogProvider
) : AuditEventFilter {
    private val logger = LoggerFactory.getLogger(LoggingFilterUseCase::class.java)

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
