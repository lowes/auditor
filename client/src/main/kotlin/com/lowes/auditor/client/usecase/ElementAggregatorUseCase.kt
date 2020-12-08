package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.AuditEvent
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSource
import com.lowes.auditor.client.entities.domain.EventType
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import reactor.core.publisher.Flux
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@Named
internal class ElementAggregatorUseCase @Inject constructor(
    private val objectDiffChecker: ObjectDiffChecker
) {
    fun aggregate(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig): Flux<AuditEvent> {
        return objectDiffChecker.diff(oldObject, newObject)
            .groupBy {
                when (it.updatedValue) {
                    null -> EventType.DELETED
                    else -> if (it.previousValue == null) EventType.CREATED else EventType.UPDATED
                }
            }.flatMap { grouped ->
                grouped.collectList().map {
                    AuditEvent(
                        id = UUID.randomUUID(),
                        applicationName = auditorEventConfig.applicationName.orEmpty(),
                        timestamp = OffsetDateTime.now(ZoneId.of("UTC")),
                        type = grouped.key(),
                        source = auditorEventConfig.eventSource ?: EventSource.SYSTEM,
                        elements = it,
                        subType = auditorEventConfig.eventSubType,
                        sourceMetadata = auditorEventConfig.eventSourceMetadata,
                        metadata = auditorEventConfig.metadata
                    )
                }
            }
    }
}
