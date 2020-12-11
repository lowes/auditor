package com.lowes.auditor.client.infrastructure.frameworks.config

import com.lowes.auditor.client.entities.interfaces.infrastructure.frameworks.ObjectDiffChecker
import com.lowes.auditor.client.infrastructure.frameworks.service.JaversWrapperService
import org.javers.core.Javers
import org.javers.core.JaversBuilder

internal object JaversModule {

    private val javers: Javers by lazy {
        JaversBuilder.javers().build()
    }

    val objectDiffChecker: ObjectDiffChecker by lazy {
        JaversWrapperService(javers)
    }
}
