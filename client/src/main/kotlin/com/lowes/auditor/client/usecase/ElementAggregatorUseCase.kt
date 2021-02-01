package com.lowes.auditor.client.usecase

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.entities.util.orDefault
import com.lowes.auditor.core.entities.domain.AuditEvent
import com.lowes.auditor.core.entities.domain.EventType
import reactor.core.publisher.Flux
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

internal class ElementAggregatorUseCase(
    private val objectDiffChecker: ObjectDiffChecker
) {
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
