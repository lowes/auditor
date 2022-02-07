package com.lowes.auditor.client.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Sets up kotest configuration for unit test module.
 */
object UnitTestSpecConfig : AbstractProjectConfig() {

    override val parallelism = Runtime.getRuntime().availableProcessors() * 2

    override val isolationMode: IsolationMode = IsolationMode.InstancePerTest

    @OptIn(ExperimentalTime::class)
    override val timeout: kotlin.time.Duration = 60.seconds
}
