package com.lowes.auditor.client.infrastructure.frameworks.mapper

import com.lowes.auditor.client.entities.domain.Element
import com.lowes.auditor.client.entities.domain.ElementMetadata
import org.javers.core.diff.changetype.ValueChange

internal object JaversMapper {

    fun toElement(valueChange: ValueChange): Element {
        return Element(
            name = valueChange.propertyName,
            previousValue = valueChange.left.toString(),
            updatedValue = valueChange.right.toString(),
            metadata = toElementMetadata(valueChange)
        )
    }

    private fun toElementMetadata(valueChange: ValueChange): ElementMetadata {
        return ElementMetadata(
            fqdn = valueChange.affectedGlobalId.masterObjectId().typeName.plus(valueChange.propertyNameWithPath)
        )
    }
}
