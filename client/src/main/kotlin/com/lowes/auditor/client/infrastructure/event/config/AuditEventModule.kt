package com.lowes.auditor.client.infrastructure.event.config

import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.infrastructure.event.service.AuditEventProducerService
import com.lowes.auditor.core.entities.util.JsonObject
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG as PRODUCER_BOOTSTRAP

/**
 * Initializes and configures underlying kafka sender to be used to send audit events
 * @property producerConfig instance of [AuditEventProducerConfig]
 */
class AuditEventModule(
    private val producerConfig: AuditEventProducerConfig
) {

    /**
     * Singleton and lazy initialized instance of [KafkaSender] used for sending audit events.
     */
    private val auditEventSender: KafkaSender<String, String> by lazy {
        KafkaSender.create(SenderOptions.create(configs(producerConfig)))
    }

    /**
     * Singleton and lazy initialized instance of [EventPublisher] that wraps underlying kafka sender
     */
    val auditEventProducerService: EventPublisher by lazy {
        AuditEventProducerService(producerConfig, auditEventSender, JsonObject.objectWriter)
    }

    /**
     * Provides configs to underlying kafka sender
     * @param producerConfig instance of [AuditEventProducerConfig]
     * @return instanc of [Map] containing kafka configs
     */
    private fun configs(producerConfig: AuditEventProducerConfig?): Map<String, String> {
        return mapOf(PRODUCER_BOOTSTRAP to producerConfig?.bootstrapServers.orEmpty())
            .plus(producerConfig?.getMergedConfig().orEmpty())
    }
}
