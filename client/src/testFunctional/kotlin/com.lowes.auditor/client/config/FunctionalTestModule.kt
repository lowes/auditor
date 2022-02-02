package com.lowes.auditor.client.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.api.Auditor
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.client.listeners.KafkaListener
import com.lowes.auditor.core.entities.util.JsonObject
import org.apache.kafka.clients.consumer.ConsumerConfig
import reactor.kafka.receiver.ReceiverOptions

/**
 * Configuration for Auditor Functional Test Module
 */
object FunctionalTestModule {
    val auditor: Auditor by lazy {
        Auditor.getInstance(
            producerConfig = AuditEventProducerConfig(
                enabled = true,
                bootstrapServers = KafkaListener.cluster.bootstrapServers,
                topic = KafkaListener.TOPIC,
                configs = mapOf("client.id" to "auditor-functional-test-client")
            ),
            auditorEventConfig = AuditorEventConfig.getDefaultInstance().copy(applicationName = "functional-test")
        )
    }

    /**
     * lazily initialized instance of Instance of [ObjectMapper] which is commonly used across modules
     */
    val objectMapper: ObjectMapper by lazy {
        JsonObject.objectMapper
    }

    /**
     * lazily initiated instance of [ReceiverOptions] which creates the Consumer with the consumerConfig
     */
    val testConsumer: ReceiverOptions<String, String> by lazy {
        ReceiverOptions.create(consumerConfigs())
    }

    /**
     * Kafka Consumer configuration
     * @return Map of Consumer Configurations
     */
    private fun consumerConfigs(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to KafkaListener.cluster.bootstrapServers,
            ConsumerConfig.CLIENT_ID_CONFIG to "auditor-functional-test-client-consumer-client",
            ConsumerConfig.GROUP_ID_CONFIG to "auditor-functional-test-client-consumer-group",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to true,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to Class.forName("org.apache.kafka.common.serialization.StringDeserializer"),
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to Class.forName("org.apache.kafka.common.serialization.StringDeserializer")
        )
    }
}
