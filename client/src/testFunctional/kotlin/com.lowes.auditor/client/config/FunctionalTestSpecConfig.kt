package com.lowes.auditor.client.config

import com.lowes.auditor.client.listeners.KafkaListener
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.spec.IsolationMode
import io.kotest.core.test.TestCaseOrder
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

/**
 * Setting Functional Test Spec Configs
 */
object FunctionalTestSpecConfig : AbstractProjectConfig() {

    override val parallelism = Runtime.getRuntime().availableProcessors() * 2

    override val isolationMode: IsolationMode = IsolationMode.SingleInstance

    @OptIn(ExperimentalTime::class)
    override val timeout = 60.seconds

    override fun listeners(): List<Listener> = listOf(KafkaListener)

    override val testCaseOrder: TestCaseOrder = TestCaseOrder.Sequential
}
