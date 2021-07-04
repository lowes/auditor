package com.lowes.auditor.client.infrastructure.frameworks.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectLogGenerator
import reactor.core.publisher.Mono

class ObjectLogGeneratorService(
    private val objectMapper: ObjectMapper
) : ObjectLogGenerator {

    override fun generate(entity: Any): Mono<String> {
        return Mono.fromCallable {
            when (entity) {
                is String -> entity
                else -> objectMapper.writeValueAsString(entity)
            }
        }
    }
}
