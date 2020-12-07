package com.lowes.auditor.client.infrastructure.event.mapper

import com.lowes.auditor.client.entities.domain.AuditEvent
import com.lowes.auditor.client.entities.domain.Element
import com.lowes.auditor.client.entities.domain.ElementMetadata
import com.lowes.auditor.client.entities.domain.EventSourceMetadata
import com.lowes.auditor.client.infrastructure.event.model.AuditEventDTO
import com.lowes.auditor.client.infrastructure.event.model.ElementDTO
import com.lowes.auditor.client.infrastructure.event.model.ElementMetadataDTO
import com.lowes.auditor.client.infrastructure.event.model.EventSourceDTO
import com.lowes.auditor.client.infrastructure.event.model.EventSourceMetadataDTO
import com.lowes.auditor.client.infrastructure.event.model.EventTypeDTO

internal object AuditEventMapper {

    fun toAuditEventDTO(auditEvent: AuditEvent): AuditEventDTO {
        return AuditEventDTO(
            id = auditEvent.id,
            applicationName = auditEvent.applicationName,
            timestamp = auditEvent.timestamp,
            type = EventTypeDTO.valueOf(auditEvent.type.value),
            source = EventSourceDTO.valueOf(auditEvent.source.value),
            elements = toElementDTO(auditEvent.elements),
            subType = auditEvent.subType,
            sourceMetadata = toSourceMetadataDTO(auditEvent.sourceMetadata),
            metadata = auditEvent.metadata
        )
    }

    fun toElementDTO(elements: List<Element>): List<ElementDTO> {
        return elements.map {
            ElementDTO(
                name = it.name,
                previousValue = it.previousValue,
                updatedValue = it.updatedValue,
                metadata = toElementMetadataDTO(it.metadata)
            )
        }
    }

    fun toElementMetadataDTO(elementMetadata: ElementMetadata?): ElementMetadataDTO {
        return ElementMetadataDTO(
            fqdn = elementMetadata?.fqdn,
            identifiers = elementMetadata?.identifiers
        )
    }

    fun toSourceMetadataDTO(sourceMetadata: EventSourceMetadata?): EventSourceMetadataDTO {
        return EventSourceMetadataDTO(
            id = sourceMetadata?.id,
            email = sourceMetadata?.email,
            name = sourceMetadata?.name
        )
    }
}
