package com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks

import com.lowes.auditor.core.entities.domain.AuditEvent
import org.slf4j.Logger
import reactor.util.context.ContextView

/**
 * Log provider interface containing functions to log audit events
 */
interface LogProvider {
    /**
     * Logs an audit event
     * @param context an instance of [ContextView] containing context relevant metadata
     * @param logger an instance of [Logger] used to log audit events
     * @param event an instance of [AuditEvent] to be logged
     */
    fun log(
        context: ContextView,
        logger: Logger,
        event: AuditEvent,
    )
}
