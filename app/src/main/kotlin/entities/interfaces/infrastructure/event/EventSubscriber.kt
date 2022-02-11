package com.lowes.auditor.app.entities.interfaces.infrastructure.event

import com.lowes.auditor.core.infrastructure.event.model.AuditEventDTO
import reactor.core.publisher.Flux

/**
 * Contract to subscribe for events to receive [AuditEventDTO] object from client application
 */
interface EventSubscriber {
    /**
     * method to consume messages from event source
     * @return `Flux<Event>`
     */
    fun consumeEvents(): Flux<AuditEventDTO>
}
