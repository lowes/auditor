package com.lowes.auditor.client.infrastructure.frameworks.config

import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.infrastructure.frameworks.service.ObjectDiffCheckerService
import com.lowes.auditor.core.entities.util.JsonObject

internal object ObjectDiffModule {

    val objectDiffChecker: ObjectDiffChecker by lazy {
        ObjectDiffCheckerService(JsonObject.objectMapper)
    }
}
