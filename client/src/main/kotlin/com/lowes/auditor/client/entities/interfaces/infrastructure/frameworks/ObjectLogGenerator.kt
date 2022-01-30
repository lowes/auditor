package com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks

import reactor.core.publisher.Mono

/**
 * Object log generator interface containing function to generate audit log for any given object
 */
interface ObjectLogGenerator {

    /**
     * Generates a textual representation of an object (mostly json except for when object type is string)
     * @param entity any object instance
     * @return mono of [String] containing textual representation
     */
    fun generate(entity: Any): Mono<String>
}
