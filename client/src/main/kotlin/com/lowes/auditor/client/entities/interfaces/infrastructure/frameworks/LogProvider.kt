package com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks

import com.lowes.auditor.core.entities.domain.AuditEvent
import org.slf4j.Logger
import reactor.util.context.ContextView

/**
 * todo
 */
interface LogProvider {

    fun log(context: ContextView, logger: Logger, event: AuditEvent)
}
