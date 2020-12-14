package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.Filters
import com.lowes.auditor.client.entities.interfaces.usecase.AuditEventFilter
import com.lowes.auditor.core.entities.domain.AuditEvent
import org.slf4j.LoggerFactory

internal class LoggingFilterUseCase : AuditEventFilter {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun filter(event: AuditEvent, filters: Filters?): Boolean {
        val loggingFilter = filters?.logging
        return if (loggingFilter?.enabled == true) {
            logger.info("Event: {}", event)
            true
        } else {
            true
        }
    }
}
