package com.lowes.auditor.client.entities.interfaces.infrastructure.event

import com.lowes.auditor.core.entities.domain.AuditEvent
import reactor.core.publisher.Flux

/**
 * Event publisher interface containing functions to publish messages to underlying event stream
 */
interface EventPublisher {
    /**
     * Publish events to an event stream.
     * @param event Flux of [AuditEvent] that needs to be sent to event stream
     * @return Flux of correlation id for the message sent to event stream
     */
    fun publishEvents(event: Flux<AuditEvent>): Flux<String>
}
