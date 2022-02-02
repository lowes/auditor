package com.lowes.auditor.client.api

import com.lowes.auditor.client.FunctionalTestSpec
import com.lowes.auditor.client.api.model.Item
import com.lowes.auditor.client.api.model.Rand
import com.lowes.auditor.client.api.model.SubObject
import com.lowes.auditor.client.api.model.User
import com.lowes.auditor.client.config.FunctionalTestModule
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.ElementFilter
import com.lowes.auditor.client.entities.domain.ElementFilterOptions
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.entities.domain.Filters
import com.lowes.auditor.client.listeners.KafkaListener
import com.lowes.auditor.core.entities.domain.AuditEvent
import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.ElementMetadata
import com.lowes.auditor.core.entities.domain.EventSource
import com.lowes.auditor.core.entities.domain.EventSourceMetadata
import com.lowes.auditor.core.entities.domain.EventSourceType
import com.lowes.auditor.core.entities.domain.EventType
import io.kotest.matchers.shouldBe
import reactor.kafka.receiver.KafkaReceiver
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Duration
import java.util.Collections

/**
 * Functional Test for [Auditor]
 */
class AuditorTest : FunctionalTestSpec() {

    init {

        val auditor = FunctionalTestModule.auditor
        val consumer = KafkaReceiver.create(
            FunctionalTestModule.testConsumer.subscription(
                Collections.singleton(
                    KafkaListener.TOPIC
                )
            )
        )
            .receive()
            .doOnNext { it.receiverOffset().acknowledge() }

        Given("Two Instance of same Object with Differences") {
            val object1 = getItem()
                .copy(
                    itemNumber = "123",
                    rand = Rand(id = "1"),
                    rand2 = Rand(id = "2"),
                    listItem = mutableListOf(Rand(id = "3"))
                )
            val object2 = getItem()
                .copy(
                    itemNumber = "1234",
                    rand = Rand(id = "12"),
                    rand2 = Rand(id = "23"),
                    listItem = mutableListOf(Rand(id = "34"))
                )
            val userObject = getUser()
            When("Audit is called with both objects ") {
                auditor.audit(object1, object2)
                Then("Assert Audit event is generated and sent to Kafka") {
                    val consumedData = consumer
                        .map {
                            FunctionalTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java)
                        }
                    StepVerifier
                        .create(consumedData)
                        .expectNextMatches {
                            it.applicationName shouldBe "functional-test"
                            it.type shouldBe EventType.UPDATED
                            it.source shouldBe EventSource(
                                type = EventSourceType.SYSTEM,
                                metadata = EventSourceMetadata()
                            )
                            it.elements?.sortedBy { it.metadata?.fqdn } shouldBe listOf(
                                Element(
                                    name = "id",
                                    updatedValue = "12",
                                    previousValue = "1",
                                    metadata = ElementMetadata(
                                        fqdn = "com.lowes.auditor.client.api.model.Item.rand.id",
                                        identifiers = null
                                    )
                                ),
                                Element(
                                    name = "id",
                                    updatedValue = "34",
                                    previousValue = "3",
                                    metadata = ElementMetadata(
                                        fqdn = "com.lowes.auditor.client.api.model.Item.listItem.0.id",
                                        identifiers = null
                                    )
                                ),
                                Element(
                                    name = "id",
                                    updatedValue = "23",
                                    previousValue = "2",
                                    metadata = ElementMetadata(
                                        fqdn = "com.lowes.auditor.client.api.model.Item.rand2.id",
                                        identifiers = null
                                    )
                                ),
                                Element(
                                    name = "itemNumber",
                                    updatedValue = "1234",
                                    previousValue = "123",
                                    metadata = ElementMetadata(
                                        fqdn = "com.lowes.auditor.client.api.model.Item.itemNumber",
                                        identifiers = null
                                    )
                                )
                            ).sortedBy { it.metadata?.fqdn }
                            true
                        }
                        .expectNoEvent(Duration.ofSeconds(2))
                        .thenCancel()
                        .verify()
                }
            }

            When("The updated object is null") {
                auditor.audit(oldObject = userObject, newObject = User())
                Then("Assert that Audit is generated and sent to Kafka") {
                    val consumedData = consumer
                        .map {
                            FunctionalTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java)
                        }
                    StepVerifier.create(consumedData)
                        .expectNextMatches {
                            it.applicationName shouldBe "functional-test"
                            it.type shouldBe EventType.DELETED
                            it.elements?.sortedBy { it.name } shouldBe listOf(
                                Element(
                                    name = "emailAddress",
                                    updatedValue = null,
                                    previousValue = "howyoudoin@friends.com",
                                    metadata = ElementMetadata(fqdn = "com.lowes.auditor.client.api.model.User.emailAddress")
                                ),
                                Element(
                                    name = "firstName",
                                    updatedValue = null,
                                    previousValue = "Joey",
                                    metadata = ElementMetadata(fqdn = "com.lowes.auditor.client.api.model.User.firstName")
                                ),
                                Element(
                                    name = "lastName",
                                    updatedValue = null,
                                    previousValue = "Tribbiani",
                                    metadata = ElementMetadata(fqdn = "com.lowes.auditor.client.api.model.User.lastName")
                                ),
                                Element(
                                    name = "nationality",
                                    updatedValue = null,
                                    previousValue = "Mexican",
                                    metadata = ElementMetadata(fqdn = "com.lowes.auditor.client.api.model.User.nationality")
                                )

                            ).sortedBy { it.name }
                            true
                        }
                        .expectNoEvent(Duration.ofSeconds(2))
                        .thenCancel()
                        .verify()
                }
            }

            When("The old object is null") {
                auditor.audit(oldObject = User(), newObject = userObject)
                Then("Assert that Audit is generated and sent to Kafka") {
                    val consumedData = consumer
                        .map {
                            FunctionalTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java)
                        }
                    StepVerifier.create(consumedData)
                        .expectNextMatches {
                            it.applicationName shouldBe "functional-test"
                            it.type shouldBe EventType.CREATED
                            it.elements?.sortedBy { it.name } shouldBe listOf(
                                // Element(name = "firstName", updatedValue = null, previousValue = "Joey", metadata = ElementMetadata(fqdn = "com"))
                                Element(
                                    name = "emailAddress",
                                    updatedValue = "howyoudoin@friends.com",
                                    previousValue = null,
                                    metadata = ElementMetadata(fqdn = "com.lowes.auditor.client.api.model.User.emailAddress")
                                ),
                                Element(
                                    name = "firstName",
                                    updatedValue = "Joey",
                                    previousValue = null,
                                    metadata = ElementMetadata(fqdn = "com.lowes.auditor.client.api.model.User.firstName")
                                ),
                                Element(
                                    name = "lastName",
                                    updatedValue = "Tribbiani",
                                    previousValue = null,
                                    metadata = ElementMetadata(fqdn = "com.lowes.auditor.client.api.model.User.lastName")
                                ),
                                Element(
                                    name = "nationality",
                                    updatedValue = "Mexican",
                                    previousValue = null,
                                    metadata = ElementMetadata(fqdn = "com.lowes.auditor.client.api.model.User.nationality")
                                )

                            ).sortedBy { it.name }
                            true
                        }
                        .expectNoEvent(Duration.ofSeconds(2))
                        .thenCancel()
                        .verify()
                }
            }

            When("Audit the new and old object with Auditor Event Config") {
                auditor.audit(
                    oldObject = object1,
                    newObject = object2,
                    auditorEventConfig = AuditorEventConfig(
                        applicationName = "override-application-name",
                        eventSource = EventSourceConfig(EventSourceType.USER),
                        eventSubType = "subType",
                        metadata = mapOf("key" to "value"),
                        filters = Filters(
                            element = ElementFilter(
                                enabled = true,
                                types = listOf("InclusionFilter", "ExclusionFilter"),
                                options = ElementFilterOptions(
                                    includes = listOf("id"),
                                    excludes = listOf(
                                        "com.lowes.auditor.client.api.model.Item.listItem.0.id",
                                        "com.lowes.auditor.client.api.model.Item.rand2.id"
                                    )
                                )
                            )
                        )
                    )
                )
                Then("Assert the Audit Event is send ") {
                    val consumedData = consumer
                        .map {
                            FunctionalTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java)
                        }
                    StepVerifier
                        .create(consumedData)
                        .expectNextMatches {
                            it.applicationName shouldBe "override-application-name"
                            it.type shouldBe EventType.UPDATED
                            it.source shouldBe EventSource(
                                type = EventSourceType.USER,
                                metadata = EventSourceMetadata()
                            )
                            it.elements shouldBe listOf(
                                Element(
                                    name = "id",
                                    updatedValue = "12",
                                    previousValue = "1",
                                    metadata = ElementMetadata(
                                        fqdn = "com.lowes.auditor.client.api.model.Item.rand.id",
                                        identifiers = null
                                    )
                                )
                            )
                            true
                        }
                        .expectNoEvent(Duration.ofSeconds(2))
                        .thenCancel()
                        .verify()
                }
            }
        }

        Given("One User Object") {
            val user1 = getUser()
            When("Log User1") {
                auditor.log(user1)
                Then("User 1 is logged") {
                    val consumedData = consumer
                        .map { FunctionalTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java) }
                    StepVerifier
                        .create(consumedData)
                        .expectNextMatches {
                            FunctionalTestModule.objectMapper.readValue(it.log, User::class.java) shouldBe user1
                            true
                        }
                        .expectNoEvent(Duration.ofSeconds(2))
                        .thenCancel()
                        .verify()
                }
            }
        }
    }

    private fun getItem(): Item {
        return Item(
            itemNumber = "123",
            model = 1,
            description = "description",
            metadata = mapOf(
                "key" to "value"
            ),
            rand = Rand(
                id = "1",
                name = "randName",
                doubleList = listOf(SubObject(value = "value", uom = "uom")),
                listString = listOf("list1", "list2"),
                mapList = mapOf(
                    "map1" to listOf(SubObject(value = "value", uom = "uom"))
                )
            ),
            rand2 = Rand(
                id = "2",
                name = "randName",
                doubleList = listOf(SubObject(value = "value", uom = "uom")),
                listString = listOf("list1", "list2"),
                mapList = mapOf(
                    "map1" to listOf(SubObject(value = "value", uom = "uom"))
                )
            ),
            listItem = mutableListOf(
                Rand(
                    id = "3",
                    name = "randName",
                    doubleList = listOf(SubObject(value = "value", uom = "uom")),
                    listString = listOf("list1", "list2"),
                    mapList = mapOf(
                        "map1" to listOf(SubObject(value = "value", uom = "uom"))
                    )
                )
            ),
            stringList = listOf("str1", "str2", "str3", "str4"),
            price = BigDecimal.valueOf(123),
            subList = listOf(SubObject(value = "value", uom = "uom")),
            subMap = mapOf("s1" to SubObject(value = "value", uom = "uom"))
        )
    }

    private fun getUser(): User {
        return User(
            firstName = "Joey",
            lastName = "Tribbiani",
            emailAddress = "howyoudoin@friends.com",
            nationality = "Mexican"
        )
    }
}
