package com.lowes.auditor.client.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.spec.IsolationMode
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

/**
 * Sets up kotest configuration for unit test module.
 */
object UnitTestSpecConfig : AbstractProjectConfig() {

    override val parallelism = Runtime.getRuntime().availableProcessors() * 2

    override val isolationMode: IsolationMode = IsolationMode.InstancePerTest

    override val invocationTimeout = 60000L

    @ExperimentalTime
    override val timeout = 720.toDuration(DurationUnit.SECONDS)

    override fun listeners(): List<Listener> = listOf()
}
