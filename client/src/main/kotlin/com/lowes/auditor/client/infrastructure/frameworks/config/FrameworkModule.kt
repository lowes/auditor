package com.lowes.auditor.client.infrastructure.frameworks.config

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectLogGenerator
import com.lowes.auditor.client.infrastructure.frameworks.service.DefaultLogProvider
import com.lowes.auditor.client.infrastructure.frameworks.service.ObjectDiffCheckerService
import com.lowes.auditor.client.infrastructure.frameworks.service.ObjectLogGeneratorService
import com.lowes.auditor.core.entities.util.JsonObject
import reactor.kafka.sender.KafkaSender

/**
 * Initializes and configures underlying frameworks used during audit even generation and proecessing
 */
object FrameworkModule {

    /**
     * Creates an instance of [ObjectDiffChecker] to enable the comparision between objects
     * @param auditorEventConfig instance of [AuditorEventConfig]
     * @return instance of [ObjectDiffChecker]
     */
    fun getObjectDiffChecker(auditorEventConfig: AuditorEventConfig): ObjectDiffChecker {
        return ObjectDiffCheckerService(JsonObject.objectMapper, auditorEventConfig)
    }

    /**
     * Singleton and lazy initialized instance of [DefaultLogProvider] used as default logger when no other log provider is present.
     */
    val defaultLogProvider: DefaultLogProvider by lazy {
        DefaultLogProvider()
    }

    /**
     * Singleton and lazy initialized instance of [ObjectLogGenerator] used for generating audit logs
     */
    val objectLogGenerator: ObjectLogGenerator by lazy {
        ObjectLogGeneratorService(JsonObject.objectMapper)
    }
}
