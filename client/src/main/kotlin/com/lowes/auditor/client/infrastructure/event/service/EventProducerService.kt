package com.lowes.auditor.client.infrastructure.event.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.entities.domain.AuditEvent
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.client.infrastructure.event.mapper.AuditEventMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import java.util.UUID

/**
 * Event producer service
 *
 * @property producerConfig
 * @property kafkaSender
 * @property objectMapper
 * @constructor Create empty Event producer service
 */
internal abstract class EventProducerService(
    private val producerConfig: AuditEventProducerConfig?,
    private val kafkaSender: KafkaSender<String, String>,
    private val objectMapper: ObjectMapper
) : EventPublisher {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Publish Message
     * @param event
     *
     * @return
     */
    override fun publishEvents(event: Flux<AuditEvent>): Flux<UUID> {
        return if (producerConfig?.enabled == true) {
            kafkaSender.send<UUID>(
                event.doOnNext { logger.info("Payload --> $it") }
                    .map {
                        SenderRecord.create(
                            ProducerRecord(
                                producerConfig.topic,
                                it.id.toString(),
                                objectMapper.writeValueAsString(AuditEventMapper.toAuditEventDTO(it))
                            ),
                            it.id
                        )
                    }
            )
                .doOnError { logger.error(it.localizedMessage) }
                .doOnNext { r -> logger.info("Success ${r.correlationMetadata()} ${r.recordMetadata().serializedValueSize()}") }
                .map { it.correlationMetadata() }
        } else {
            Flux.empty()
        }
    }
}
