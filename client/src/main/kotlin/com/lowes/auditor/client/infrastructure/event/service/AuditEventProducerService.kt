package com.lowes.auditor.client.infrastructure.event.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import reactor.kafka.sender.KafkaSender

/**
 * Audit event producer service
 *
 * @constructor Create Empty Intake Producer
 */
internal class AuditEventProducerService(
    producerConfig: AuditEventProducerConfig,
    auditEventSender: KafkaSender<String, String>,
    auditorObjectMapper: ObjectMapper
) : EventProducerService(producerConfig, auditEventSender, auditorObjectMapper)
