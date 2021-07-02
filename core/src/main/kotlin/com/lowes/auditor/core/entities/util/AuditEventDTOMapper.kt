package com.lowes.auditor.core.entities.util

import com.lowes.auditor.core.entities.domain.AuditEvent
import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.ElementMetadata
import com.lowes.auditor.core.entities.domain.EventSourceMetadata
import com.lowes.auditor.core.infrastructure.event.model.AuditEventDTO
import com.lowes.auditor.core.infrastructure.event.model.ElementDTO
import com.lowes.auditor.core.infrastructure.event.model.ElementMetadataDTO
import com.lowes.auditor.core.infrastructure.event.model.EventSourceDTO
import com.lowes.auditor.core.infrastructure.event.model.EventSourceMetadataDTO
import com.lowes.auditor.core.infrastructure.event.model.EventSourceTypeDTO
import com.lowes.auditor.core.infrastructure.event.model.EventTypeDTO

object AuditEventDTOMapper {

    fun toAuditEventDTO(auditEvent: AuditEvent): AuditEventDTO {
        return AuditEventDTO(
            id = auditEvent.id,
            applicationName = auditEvent.applicationName,
            timestamp = auditEvent.timestamp,
            type = EventTypeDTO.valueOf(auditEvent.type.value),
            source = EventSourceDTO(
                type = auditEvent.source.type.value.let { EventSourceTypeDTO.valueOf(it) },
                metadata = toSourceMetadataDTO(auditEvent.source.metadata)
            ),
            elements = toElementDTO(auditEvent.elements),
            subType = auditEvent.subType,
            metadata = auditEvent.metadata,
            log = auditEvent.log
        )
    }

    private fun toElementDTO(elements: List<Element>?): List<ElementDTO>? {
        return elements?.map {
            ElementDTO(
                name = it.name,
                previousValue = it.previousValue,
                updatedValue = it.updatedValue,
                metadata = toElementMetadataDTO(it.metadata)
            )
        }
    }

    private fun toElementMetadataDTO(elementMetadata: ElementMetadata?): ElementMetadataDTO {
        return ElementMetadataDTO(
            fqdn = elementMetadata?.fqdn,
            identifiers = elementMetadata?.identifiers
        )
    }

    private fun toSourceMetadataDTO(sourceMetadata: EventSourceMetadata?): EventSourceMetadataDTO {
        return EventSourceMetadataDTO(
            id = sourceMetadata?.id,
            email = sourceMetadata?.email,
            name = sourceMetadata?.name
        )
    }
}
