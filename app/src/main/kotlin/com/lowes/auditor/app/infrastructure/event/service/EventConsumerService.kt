package com.lowes.auditor.app.infrastructure.event.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.app.entities.interfaces.infrastructure.event.EventSubscriber
import com.lowes.auditor.app.infrastructure.event.config.AuditEventConsumerConfig
import com.lowes.auditor.core.infrastructure.event.model.AuditEventDTO
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import java.util.Collections

/**
 * Event consumer service responsible for receiving events from kafka
 *
 * @property consumerConfig of type [AuditEventConsumerConfig]
 * @property auditEventReceiver of type [ReceiverOptions]
 * @property objectMapper of type [ObjectMapper]
 * @see EventSubscriber
 */
abstract class EventConsumerService(
    private val consumerConfig: AuditEventConsumerConfig?,
    private val auditEventReceiver: ReceiverOptions<String, String>,
    private val objectMapper: ObjectMapper
) : EventSubscriber {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Consume events from kafka if enabled flag is set to true and map it to [AuditEventDTO]
     *
     * @return
     * @see EventSubscriber.consumeEvents
     */
    override fun consumeEvents(): Flux<AuditEventDTO> {
        return if (consumerConfig?.enabled == true) {
            logger.info("${consumerConfig.javaClass.simpleName} is ${consumerConfig.enabled}, consuming events")
            KafkaReceiver.create(auditEventReceiver.subscription(Collections.singleton(consumerConfig.topic)))
                .receive()
                .doOnNext { logger.info("Received msg ${it.value()}") }
                .flatMap { record ->
                    val event = objectMapper.readValue(record.value(), AuditEventDTO::class.java)
                    consume(event)
                        .doOnNext { record.receiverOffset().acknowledge() }
                        .thenReturn(event)
                }
        } else {
            logger.info("${consumerConfig?.javaClass?.simpleName} is ${consumerConfig?.enabled}, skipping events")
            Flux.empty()
        }
    }

    /**
     * consume each event
     * @param event of type [AuditEventDTO]
     * @return `Mono<AuditEventDTO>`
     */
    abstract fun consume(event: AuditEventDTO): Mono<AuditEventDTO>
}
