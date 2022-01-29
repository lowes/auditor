package com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks

import com.lowes.auditor.core.entities.domain.Element
import reactor.core.publisher.Flux

/**
 * Object diff checker interface containing functions to perform diff between two objects
 */
interface ObjectDiffChecker {

    /**
     * Performs diff between two objects
     * @param objectOne previous instance of the object to be compared against
     * @param objectTwo latest instance of the object that needs to be audited
     * @return flux of [Element] outlining the difference between two objects
     */
    fun diff(objectOne: Any?, objectTwo: Any?): Flux<Element>
}
