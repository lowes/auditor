package com.lowes.auditor.client.config

import com.lowes.auditor.client.listeners.KafkaListener
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.core.listeners.Listener
import io.kotest.core.spec.IsolationMode
import io.kotest.core.test.TestCaseOrder
import io.kotest.spring.SpringAutowireConstructorExtension

/**
 * Sets up kotest configuration for Integration Test Module
 */
object IntegrationTestSpecConfig : AbstractProjectConfig() {

    override val parallelism = Runtime.getRuntime().availableProcessors() * 2

    override val isolationMode: IsolationMode = IsolationMode.SingleInstance

    override val invocationTimeout = 10000L

    override fun extensions(): List<Extension> = listOf(SpringAutowireConstructorExtension)

    override fun listeners(): List<Listener> = listOf(KafkaListener)

    override val testCaseOrder: TestCaseOrder = TestCaseOrder.Sequential
}
