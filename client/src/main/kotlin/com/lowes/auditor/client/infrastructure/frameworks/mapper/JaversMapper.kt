package com.lowes.auditor.client.infrastructure.frameworks.mapper

import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.ElementMetadata
import org.javers.core.diff.changetype.PropertyChange
import org.javers.core.diff.changetype.ValueChange
import org.javers.core.diff.changetype.container.ContainerChange
import org.javers.core.diff.changetype.map.MapChange

internal object JaversMapper {

    fun toElement(change: PropertyChange): Element {
        return Element(
            name = change.propertyName,
            previousValue = getPreviousValue(change),
            updatedValue = getUpdatedValue(change),
            metadata = toElementMetadata(change)
        )
    }

    private fun toElementMetadata(change: PropertyChange): ElementMetadata {
        return ElementMetadata(
            fqdn = change.affectedGlobalId.masterObjectId().typeName.plus(".").plus(change.propertyNameWithPath)
        )
    }

    private fun getPreviousValue(change: PropertyChange): String? {
        return when (change) {
            is ValueChange -> change.left.toString()
            is MapChange -> change.entryRemovedChanges.joinToString { "${it.key}:${it.value}" }
            is ContainerChange -> change.removedValues.joinToString()
            else -> null
        }
    }

    private fun getUpdatedValue(change: PropertyChange): String? {
        return when (change) {
            is ValueChange -> change.right.toString()
            is MapChange -> change.entryAddedChanges.joinToString { "${it.key}:${it.value}" }
            is ContainerChange -> change.valueAddedChanges.joinToString()
            else -> null
        }
    }
}
