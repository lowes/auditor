package com.lowes.auditor.client.infrastructure.event.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.listeners.KafkaListener
import com.lowes.auditor.core.entities.util.JsonObject
import org.apache.kafka.clients.consumer.ConsumerConfig
import reactor.kafka.receiver.ReceiverOptions

/**
 * Sets up configuration for Auditor Event Module
 */
object EventTestModule {

    /**
     * lazily initialized instance of Instance of [ObjectMapper] which is commonly used across modules
     */
    val objectMapper: ObjectMapper by lazy {
        JsonObject.objectMapper
    }

    /**
     * lazily initialized instance of [EventPublisher] which uses the testContainerized instances of Kafka
     */
    val auditEventProducerService: EventPublisher by lazy {
        AuditEventModule(
            AuditEventProducerConfig(
                enabled = true,
                bootstrapServers = KafkaListener.cluster.bootstrapServers,
                topic = KafkaListener.TOPIC
            )
        ).auditEventProducerService
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
            ConsumerConfig.CLIENT_ID_CONFIG to "auditor-client-consumer-client",
            ConsumerConfig.GROUP_ID_CONFIG to "auditor-client-consumer-group",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to Class.forName("org.apache.kafka.common.serialization.StringDeserializer"),
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to Class.forName("org.apache.kafka.common.serialization.StringDeserializer")
        )
    }
}
