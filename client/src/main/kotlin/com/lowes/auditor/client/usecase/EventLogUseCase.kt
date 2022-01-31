package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectLogGenerator
import com.lowes.auditor.client.entities.util.orDefault
import com.lowes.auditor.core.entities.domain.AuditEvent
import com.lowes.auditor.core.entities.domain.EventType
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

/**
 * Contains use cases for generating audit event logs.
 * @property objectLogGenerator instance of [ObjectLogGenerator]
 */
class EventLogUseCase(
    private val objectLogGenerator: ObjectLogGenerator
) {
    /**
     * Generates [AuditEvent] from the entity that needs to logged. Also, applies metadata and other relevant information from [auditorEventConfig]
     * @param entity instance of [Any]
     * @param auditorEventConfig instance of [AuditorEventConfig]
     * @return mono of [AuditEvent]
     */
    fun logEvent(entity: Any, auditorEventConfig: AuditorEventConfig): Mono<AuditEvent> {
        return objectLogGenerator.generate(entity)
            .map {
                AuditEvent(
                    id = UUID.randomUUID(),
                    applicationName = auditorEventConfig.applicationName.orDefault("NOT_CONFIGURED"),
                    timestamp = OffsetDateTime.now(ZoneId.of("UTC")),
                    type = EventType.CREATED,
                    source = auditorEventConfig.eventSource.orDefault(EventSourceConfig()).toEventSource(),
                    elements = null,
                    subType = auditorEventConfig.eventSubType,
                    metadata = auditorEventConfig.metadata,
                    log = it
                )
            }
    }
}
