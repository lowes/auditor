package com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks

import com.lowes.auditor.core.entities.domain.Element
import reactor.core.publisher.Flux

interface ObjectDiffChecker {

    fun diff(objectOne: Any?, objectTwo: Any?): Flux<Element>
}
