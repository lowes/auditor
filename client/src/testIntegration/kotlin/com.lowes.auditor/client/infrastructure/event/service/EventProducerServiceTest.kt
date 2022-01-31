package com.lowes.auditor.client.infrastructure.event.service

import com.lowes.auditor.client.IntegrationTestSpec
import com.lowes.auditor.client.infrastructure.event.config.EventTestModule
import com.lowes.auditor.client.listeners.KafkaListener
import com.lowes.auditor.core.entities.domain.AuditEvent
import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.ElementMetadata
import com.lowes.auditor.core.entities.domain.EventSource
import com.lowes.auditor.core.entities.domain.EventSourceMetadata
import com.lowes.auditor.core.entities.domain.EventSourceType
import com.lowes.auditor.core.entities.domain.EventType
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import java.util.Collections
import java.util.UUID

/**
 * Integration test for [EventProducerService]
 */
class EventProducerServiceTest : IntegrationTestSpec() {
    init {
        Given("Test Producer Event") {
            val consumer = KafkaReceiver.create(EventTestModule.testConsumer.subscription(Collections.singleton(KafkaListener.TOPIC)))
            val randomId = UUID.randomUUID()
            val auditEvent = AuditEvent(
                id = randomId,
                applicationName = "clientApp",
                timestamp = OffsetDateTime.MAX,
                type = EventType.CREATED,
                source = EventSource(
                    type = EventSourceType.USER,
                    metadata = EventSourceMetadata(
                        id = "1",
                        name = "name",
                        email = "name@email.com"
                    )
                ),
                subType = "subType",
                metadata = mapOf("key" to "value"),
                elements = listOf(
                    Element(
                        name = "elementName",
                        updatedValue = "updatedValue",
                        previousValue = "prevValue",
                        metadata = ElementMetadata(
                            fqdn = "fqdn",
                            identifiers = mapOf("key" to "value")
                        )
                    )
                )
            )
            When("Auditor produces an Event") {
                val producedAuditEvent = EventTestModule.auditEventProducerService.publishEvents(Flux.just(auditEvent))
                Then("Assert that Audit Even is successfully produced") {
                    StepVerifier
                        .create(producedAuditEvent)
                        .expectNext(randomId.toString())
                        .verifyComplete()
                }
            }
            And("verify consumer") {
                When("Consumer consumes the data") {
                    val consumedData = consumer.receive()
                        .map {
                            it.receiverOffset().acknowledge()
                            EventTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java)
                        }
                    Then("Assert that Audit Event is consumed Successfully") {
                        StepVerifier
                            .create(consumedData)
                            .expectNext(auditEvent)
                            .thenCancel()
                            .verify()
                    }
                }
            }
        }
    }
}
