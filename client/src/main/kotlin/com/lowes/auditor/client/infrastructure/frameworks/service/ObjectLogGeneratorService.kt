package com.lowes.auditor.client.infrastructure.frameworks.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectLogGenerator
import reactor.core.publisher.Mono

/**
 * Provides implementation definitions for [ObjectLogGenerator]
 * @property objectMapper instance of [ObjectMapper]
 * @see ObjectLogGenerator
 */
class ObjectLogGeneratorService(
    private val objectMapper: ObjectMapper
) : ObjectLogGenerator {

    /**
     * Generates audit logs for an object using underlying [ObjectMapper]
     * @param entity instance of [Any]
     * @return mono of textual representation of the entity
     */
    override fun generate(entity: Any): Mono<String> {
        return Mono.fromCallable {
            when (entity) {
                is String -> entity
                else -> objectMapper.writeValueAsString(entity)
            }
        }
    }
}
