package com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks

import reactor.core.publisher.Mono

interface ObjectLogGenerator {

    fun generate(entity: Any): Mono<String>
}
