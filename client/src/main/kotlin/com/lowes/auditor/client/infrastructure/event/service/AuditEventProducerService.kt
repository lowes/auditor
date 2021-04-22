package com.lowes.auditor.client.infrastructure.event.service

import com.fasterxml.jackson.databind.ObjectWriter
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import reactor.kafka.sender.KafkaSender

/**
 * Audit event producer service
 *
 * @constructor Create Empty Intake Producer
 */
class AuditEventProducerService(
    producerConfig: AuditEventProducerConfig,
    auditEventSender: KafkaSender<String, String>,
    auditorObjectWriter: ObjectWriter
) : EventProducerService(producerConfig, auditEventSender, auditorObjectWriter)
