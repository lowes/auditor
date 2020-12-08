package com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks

import com.lowes.auditor.client.entities.domain.Element
import reactor.core.publisher.Flux

internal interface ObjectDiffChecker {

    fun diff(objectOne: Any?, objectTwo: Any?): Flux<Element>
}
