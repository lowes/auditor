package com.lowes.auditor.client.infrastructure.event.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import reactor.kafka.sender.KafkaSender
import javax.inject.Named

/**
 * Audit event producer service
 *
 * @constructor Create Empty Intake Producer
 */
@Named
internal class AuditEventProducerService constructor(
    producerConfig: AuditEventProducerConfig,
    auditEventSender: KafkaSender<String, String>,
    objectMapper: ObjectMapper
) : EventProducerService(producerConfig, auditEventSender, objectMapper)
