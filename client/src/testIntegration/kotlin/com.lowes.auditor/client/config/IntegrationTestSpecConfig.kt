package com.lowes.auditor.client.config

import com.lowes.auditor.client.listeners.KafkaListener
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.IsolationMode
import io.kotest.core.test.TestCaseOrder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Sets up kotest configuration for Integration Test Module
 */
object IntegrationTestSpecConfig : AbstractProjectConfig() {

    override val parallelism = Runtime.getRuntime().availableProcessors() * 2

    override val isolationMode: IsolationMode = IsolationMode.SingleInstance

    @OptIn(ExperimentalTime::class)
    override val timeout: Duration = 60.seconds

    override fun extensions(): List<Extension> {
        return listOf(KafkaListener)
    }

    override val testCaseOrder: TestCaseOrder = TestCaseOrder.Sequential
}
