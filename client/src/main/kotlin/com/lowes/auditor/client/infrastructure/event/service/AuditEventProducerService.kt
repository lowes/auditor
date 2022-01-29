package com.lowes.auditor.client.infrastructure.event.service

import com.fasterxml.jackson.databind.ObjectWriter
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import reactor.kafka.sender.KafkaSender

/**
 * Audit Event producer service responsible for sending events to kafka
 * @property producerConfig instance of [AuditEventProducerConfig] containing producer configs for kafka
 * @property auditEventSender instance of [KafkaSender] used for sending message to kafka
 * @property auditorObjectWriter instance of [ObjectWriter] used for serializing message to kafka
 * @see [EventProducerService]
 */
class AuditEventProducerService(
        private val producerConfig: AuditEventProducerConfig,
        private val auditEventSender: KafkaSender<String, String>,
        private val auditorObjectWriter: ObjectWriter
) : EventProducerService(producerConfig, auditEventSender, auditorObjectWriter)
