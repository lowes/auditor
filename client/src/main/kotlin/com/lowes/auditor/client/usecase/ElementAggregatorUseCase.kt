package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.entities.util.orDefault
import com.lowes.auditor.core.entities.domain.AuditEvent
import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.EventType
import reactor.core.publisher.Flux
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

/**
 * Compares old and new objects to generates [AuditEvent]
 * @property objectDiffChecker instanc of [ObjectDiffChecker] that performs the actual diff between two objects
 */
class ElementAggregatorUseCase(
    private val objectDiffChecker: ObjectDiffChecker
) {
    /**
     * Compares old and new objects to find delta changes called [Element] which is then aggregated to [AuditEvent]
     * @param oldObject previous instance of the object to be compared against
     * @param newObject latest instance of the object that needs to be audited
     * @param auditorEventConfig an instance of [AuditorEventConfig] containing audit related configurations
     * @return flux of generated [AuditEvent]
     */
    fun aggregate(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig): Flux<AuditEvent> {
        return objectDiffChecker.diff(oldObject, newObject)
            .groupBy {
                when (it.updatedValue) {
                    null -> EventType.DELETED
                    else -> if (it.previousValue.isNullOrBlank()) EventType.CREATED else EventType.UPDATED
                }
            }.flatMap { grouped ->
                grouped.collectList().map {
                    AuditEvent(
                        id = UUID.randomUUID(),
                        applicationName = auditorEventConfig.applicationName.orDefault("NOT_CONFIGURED"),
                        timestamp = OffsetDateTime.now(ZoneId.of("UTC")),
                        type = grouped.key(),
                        source = auditorEventConfig.eventSource.orDefault(EventSourceConfig()).toEventSource(),
                        elements = it,
                        subType = auditorEventConfig.eventSubType,
                        metadata = auditorEventConfig.metadata
                    )
                }
            }
    }
}
