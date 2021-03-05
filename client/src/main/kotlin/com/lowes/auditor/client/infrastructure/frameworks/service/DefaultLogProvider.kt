package com.lowes.auditor.client.infrastructure.frameworks.service

import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.LogProvider
import com.lowes.auditor.core.entities.domain.AuditEvent
import org.slf4j.Logger
import reactor.util.context.ContextView

class DefaultLogProvider : LogProvider {

    override fun log(context: ContextView, logger: Logger, event: AuditEvent) {
        logger.info("Event: {}", event)
    }
}
