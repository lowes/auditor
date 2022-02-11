package com.lowes.auditor.app.entities.interfaces.infrastructure.repository

import com.lowes.auditor.core.entities.domain.AuditEvent
import reactor.core.publisher.Mono

/**
 * Repository service exposing api(s) to interact with underlying database.
 */
interface RepositoryService {

    /**
     * Saves the data to underlying database
     * @param event instance of [AuditEvent] that needs to be saved to repository.
     * @return mono of saved [AuditEvent]
     */
    fun save(event: AuditEvent): Mono<AuditEvent>
}
