package com.lowes.auditor.client.infrastructure.event.service

import com.fasterxml.jackson.databind.ObjectWriter
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.core.entities.domain.AuditEvent
import com.lowes.auditor.core.entities.util.AuditEventDTOMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord

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
    private val auditorObjectWriter: ObjectWriter
) : EventPublisher {
    private val logger = LoggerFactory.getLogger(EventProducerService::class.java)

    /**
     * Publish Message
     * @param event
     *
     * @return
     */
    override fun publishEvents(event: Flux<AuditEvent>): Flux<String> {
        return if (producerConfig?.enabled == true) {
            kafkaSender.send<String>(
                event.doOnNext { logger.debug("Payload: {}", it) }
                    .map {
                        SenderRecord.create(
                            ProducerRecord(
                                producerConfig.topic,
                                it.id.toString(),
                                auditorObjectWriter.writeValueAsString(AuditEventDTOMapper.toAuditEventDTO(it))
                            ),
                            it.id.toString()
                        )
                    }
            )
                .doOnError { logger.error(it.localizedMessage) }
                .doOnNext { r ->
                    logger.info(
                        "Success, correlationMetadata:{}, serializedValueSize:{}",
                        r.correlationMetadata(),
                        r.recordMetadata().serializedValueSize()
                    )
                }
                .map { it.correlationMetadata() }
        } else {
            Flux.empty()
        }
    }
}
