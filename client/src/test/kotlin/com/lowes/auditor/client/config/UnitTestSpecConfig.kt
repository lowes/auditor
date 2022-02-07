package com.lowes.auditor.client.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import java.time.Duration

/**
 * Sets up kotest configuration for unit test module.
 */
object UnitTestSpecConfig : AbstractProjectConfig() {

    override val parallelism = Runtime.getRuntime().availableProcessors() * 2

    override val isolationMode: IsolationMode = IsolationMode.InstancePerTest

    override val invocationTimeout = 60000L

    override val timeout = Duration.ofSeconds(10)

}
