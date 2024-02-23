package com.lowes.auditor.app.infrastructure.event.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.app.infrastructure.event.config.AuditEventConsumerConfig
import com.lowes.auditor.core.infrastructure.event.model.AuditEventDTO
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kafka.receiver.ReceiverOptions

/**
 * Consumes audit event data from kafka
 *
 * @property auditBaseConsumer instance [AuditBaseConsumer]
 * @param consumerConfig instance [AuditEventConsumerConfig]
 * @param auditEventReceiver instance [ReceiverOptions]
 * @param objectMapper instance [ObjectMapper]
 * @see EventConsumerService
 */
@Service
class AuditEventConsumerService(
    private val auditBaseConsumer: AuditBaseConsumer,
    consumerConfig: AuditEventConsumerConfig?,
    auditEventReceiver: ReceiverOptions<String, String>,
    objectMapper: ObjectMapper,
) : EventConsumerService(consumerConfig, auditEventReceiver, objectMapper) {
    /**
     * Consumes audit event data from kafka and process the incoming message via [AuditBaseConsumer]
     * @return mono of [AuditEventDTO]
     * @see EventConsumerService.consume
     */
    override fun consume(event: AuditEventDTO): Mono<AuditEventDTO> {
        return auditBaseConsumer.consumeMessage(event)
    }
}
