package com.lowes.auditor.client.infrastructure.event.config

import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.util.JsonObject
import com.lowes.auditor.client.infrastructure.event.service.AuditEventProducerService
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG as PRODUCER_BOOTSTRAP

internal class AuditEventModule(
    producerConfig: AuditEventProducerConfig
) {

    val auditEventSender: KafkaSender<String, String> by lazy {
        KafkaSender.create(SenderOptions.create(configs(producerConfig)))
    }

    val auditEventProducerService: EventPublisher by lazy {
        AuditEventProducerService(producerConfig, auditEventSender, JsonObject.objectWriter)
    }

    private fun configs(producerConfig: AuditEventProducerConfig?): Map<String, String?> {
        return mapOf(PRODUCER_BOOTSTRAP to producerConfig?.bootstrapServers)
            .plus(producerConfig?.configs ?: mapOf())
    }
}
