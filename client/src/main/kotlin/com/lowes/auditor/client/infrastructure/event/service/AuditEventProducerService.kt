package com.lowes.auditor.client.infrastructure.event.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import reactor.kafka.sender.KafkaSender
import javax.inject.Named

/**
 * Intake Producer
 *
 * @constructor Create Empty Intake Producer
 */
@Named
internal class AuditEventProducerService constructor(
    producerConfig: AuditEventProducerConfig,
    intakeSender: KafkaSender<String, String>,
    objectMapper: ObjectMapper
) : EventProducerService(producerConfig, intakeSender, objectMapper)
