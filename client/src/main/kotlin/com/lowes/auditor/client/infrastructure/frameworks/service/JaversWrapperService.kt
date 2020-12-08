package com.lowes.auditor.client.infrastructure.frameworks.service

import com.lowes.auditor.client.entities.domain.Element
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.infrastructure.frameworks.mapper.JaversMapper
import org.javers.core.Javers
import org.javers.core.diff.changetype.ValueChange
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import javax.inject.Inject
import javax.inject.Named

@Named
internal class JaversWrapperService @Inject constructor(
    private val javers: Javers
) : ObjectDiffChecker {

    override fun diff(objectOne: Any?, objectTwo: Any?): Flux<Element> {
        val diff = javers.compare(objectOne, objectTwo)
        return diff.getChangesByType(ValueChange::class.java).filter {
            it.right != null
        }.toFlux().map {
            JaversMapper.toElement(it)
        }
    }
}
