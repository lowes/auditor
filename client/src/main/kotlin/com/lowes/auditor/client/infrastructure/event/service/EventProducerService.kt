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
 * Event producer service responsible for sending events to kafka
 * @property producerConfig instance of [AuditEventProducerConfig] containing producer configs for kafka
 * @property kafkaSender instance of [KafkaSender] used for sending message to kafka
 * @property auditorObjectWriter instance of [ObjectWriter] used for serializing message to kafka
 * @see [EventPublisher]
 */
abstract class EventProducerService(
    private val producerConfig: AuditEventProducerConfig?,
    private val kafkaSender: KafkaSender<String, String>,
    private val auditorObjectWriter: ObjectWriter,
) : EventPublisher {
    private val logger = LoggerFactory.getLogger(EventProducerService::class.java)

    /**
     * Publish events to underlying kafka stream
     * @see [EventPublisher.publishEvents]
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
                                auditorObjectWriter.writeValueAsString(AuditEventDTOMapper.toAuditEventDTO(it)),
                            ),
                            it.id.toString(),
                        )
                    },
            )
                .doOnError { logger.error(it.localizedMessage) }
                .doOnNext { r ->
                    logger.info(
                        "Success, correlationMetadata:{}, serializedValueSize:{}",
                        r.correlationMetadata(),
                        r.recordMetadata().serializedValueSize(),
                    )
                }
                .map { it.correlationMetadata() }
        } else {
            Flux.empty()
        }
    }
}
