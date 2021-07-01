package com.lowes.auditor.client.infrastructure.frameworks.config

import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectLogGenerator
import com.lowes.auditor.client.infrastructure.frameworks.service.DefaultLogProvider
import com.lowes.auditor.client.infrastructure.frameworks.service.ObjectDiffCheckerService
import com.lowes.auditor.client.infrastructure.frameworks.service.ObjectLogGeneratorService
import com.lowes.auditor.core.entities.util.JsonObject

object FrameworkModule {

    val objectDiffChecker: ObjectDiffChecker by lazy {
        ObjectDiffCheckerService(JsonObject.objectMapper)
    }

    val defaultLogProvider: DefaultLogProvider by lazy {
        DefaultLogProvider()
    }

    val objectLogGenerator: ObjectLogGenerator by lazy {
        ObjectLogGeneratorService(JsonObject.objectMapper)
    }
}
