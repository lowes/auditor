package com.lowes.auditor.client.entities.interfaces.infrastructure.event

import com.lowes.auditor.core.entities.domain.AuditEvent
import reactor.core.publisher.Flux

/**
 * Event Publisher
 * Contract to publish events
 */
interface EventPublisher {
    /**
     * publishMessage
     * @param event - Flux<Event>
     *
     * @return
     * publish message contract method
     */
    fun publishEvents(event: Flux<AuditEvent>): Flux<String>
}
