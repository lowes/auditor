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

        val noEventDuration = Duration.ofSeconds(2)

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
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_RAND_ID,
                                        identifiers = null
                                    )
                                ),
                                Element(
                                    name = "id",
                                    updatedValue = "34",
                                    previousValue = "3",
                                    metadata = ElementMetadata(
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_LIST_ITEM_0_ID,
                                        identifiers = null
                                    )
                                ),
                                Element(
                                    name = "id",
                                    updatedValue = "23",
                                    previousValue = "2",
                                    metadata = ElementMetadata(
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_RAND_2_ID,
                                        identifiers = null
                                    )
                                ),
                                Element(
                                    name = "itemNumber",
                                    updatedValue = "1234",
                                    previousValue = "123",
                                    metadata = ElementMetadata(
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_ITEM_NUMBER,
                                        identifiers = null
                                    )
                                )
                            ).sortedBy { it.metadata?.fqdn }
                            true
                        }
                        .expectNoEvent(noEventDuration)
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
                            it.type shouldBe EventType.DELETED
                            it.elements?.sortedBy { it.name } shouldBe listOf(
                                Element(
                                    name = "emailAddress",
                                    updatedValue = null,
                                    previousValue = getUser().emailAddress,
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_EMAIL_ADDRESS)
                                ),
                                Element(
                                    name = "firstName",
                                    updatedValue = null,
                                    previousValue = getUser().firstName,
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_FIRST_NAME)
                                ),
                                Element(
                                    name = "lastName",
                                    updatedValue = null,
                                    previousValue = getUser().lastName,
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_LAST_NAME)
                                ),
                                Element(
                                    name = "nationality",
                                    updatedValue = null,
                                    previousValue = getUser().nationality,
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_NATIONALITY)
                                )

                            ).sortedBy { it.name }
                            true
                        }
                        .expectNoEvent(noEventDuration)
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
                            it.type shouldBe EventType.CREATED
                            it.elements?.sortedBy { it.name } shouldBe listOf(
                                Element(
                                    name = "emailAddress",
                                    updatedValue = "howyoudoin@friends.com",
                                    previousValue = null,
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_EMAIL_ADDRESS)
                                ),
                                Element(
                                    name = "firstName",
                                    updatedValue = "Joey",
                                    previousValue = null,
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_FIRST_NAME)
                                ),
                                Element(
                                    name = "lastName",
                                    updatedValue = "Tribbiani",
                                    previousValue = null,
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_LAST_NAME)
                                ),
                                Element(
                                    name = "nationality",
                                    updatedValue = "Mexican",
                                    previousValue = null,
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_NATIONALITY)
                                )

                            ).sortedBy { it.name }
                            true
                        }
                        .expectNoEvent(noEventDuration)
                        .thenCancel()
                        .verify()
                }
            }

            When("Audit the new and old object with Inclusion filter for Rand.id") {
                auditor.audit(
                    oldObject = object1,
                    newObject = object2,
                    auditorEventConfig = AuditorEventConfig(
                        applicationName = OVERRIDE_APPLICATION_NAME,
                        eventSource = EventSourceConfig(EventSourceType.USER),
                        eventSubType = "subType",
                        metadata = mapOf("key" to "value"),
                        filters = Filters(
                            element = ElementFilter(
                                enabled = true,
                                types = listOf("InclusionFilter"),
                                options = ElementFilterOptions(
                                    includes = listOf(COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_RAND_ID)
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
                            it.applicationName shouldBe OVERRIDE_APPLICATION_NAME
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
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_RAND_ID,
                                        identifiers = null
                                    )
                                )
                            )
                            true
                        }
                        .expectNoEvent(noEventDuration)
                        .thenCancel()
                        .verify()
                }
            }

            When("Audit the new and old object with Exclusion filter for Rand.id") {
                auditor.audit(
                    oldObject = object1,
                    newObject = object2,
                    auditorEventConfig = AuditorEventConfig(
                        applicationName = OVERRIDE_APPLICATION_NAME,
                        eventSource = EventSourceConfig(EventSourceType.USER),
                        eventSubType = "subType",
                        metadata = mapOf("key" to "value"),
                        filters = Filters(
                            element = ElementFilter(
                                enabled = true,
                                types = listOf("ExclusionFilter"),
                                options = ElementFilterOptions(
                                    excludes = listOf(COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_RAND_ID)
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
                            it.applicationName shouldBe OVERRIDE_APPLICATION_NAME
                            it.type shouldBe EventType.UPDATED
                            it.source shouldBe EventSource(
                                type = EventSourceType.USER,
                                metadata = EventSourceMetadata()
                            )
                            it.elements?.sortedBy { it.metadata?.fqdn } shouldBe listOf(
                                Element(
                                    name = "id",
                                    updatedValue = "34",
                                    previousValue = "3",
                                    metadata = ElementMetadata(
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_LIST_ITEM_0_ID
                                    )
                                ),
                                Element(
                                    name = "id",
                                    updatedValue = "23",
                                    previousValue = "2",
                                    metadata = ElementMetadata(
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_RAND_2_ID
                                    )
                                ),
                                Element(
                                    name = "itemNumber",
                                    updatedValue = "1234",
                                    previousValue = "123",
                                    metadata = ElementMetadata(
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_ITEM_NUMBER
                                    )
                                )
                            ).sortedBy { it.metadata?.fqdn }
                            true
                        }
                        .expectNoEvent(noEventDuration)
                        .thenCancel()
                        .verify()
                }
            }

            When("Updating a list of Object") {
                auditor.audit(
                    oldObject = getItem()
                        .copy(
                            listItem = mutableListOf(Rand(id = "1"), Rand(id = "2")),
                        ),
                    newObject = getItem()
                        .copy(
                            listItem = mutableListOf(Rand(id = "1"), Rand(id = "3"))
                        )
                )
                Then("Assert that the mutable List of Rand object is updated") {
                    val consumedData = consumer
                        .map {
                            FunctionalTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java)
                        }
                    StepVerifier
                        .create(consumedData)
                        .expectNextMatches {
                            it.type shouldBe EventType.UPDATED
                            it.elements?.sortedBy { it.metadata?.fqdn } shouldBe listOf(
                                Element(
                                    name = "id",
                                    updatedValue = "3",
                                    previousValue = "2",
                                    metadata = ElementMetadata(
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_LIST_ITEM_1_ID
                                    )
                                ),
                            ).sortedBy { it.metadata?.fqdn }
                            true
                        }
                        .expectNoEvent(noEventDuration)
                        .thenCancel()
                        .verify()
                }
            }

            When("Updating a map key value") {
                auditor.audit(
                    oldObject = getItem()
                        .copy(
                            metadata = mapOf("key1" to "value1")
                        ),
                    newObject = getItem()
                        .copy(
                            metadata = mapOf("key2" to "value2")
                        )
                )
                Then("Assert that a Create and Delete Audit Event is sent") {
                    val consumedData = consumer
                        .map {
                            FunctionalTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java)
                        }
                    StepVerifier
                        .create(consumedData)
                        .expectNextMatches {
                            it.type shouldBe EventType.CREATED
                            it.elements shouldBe listOf(
                                Element(
                                    name = "key2",
                                    updatedValue = "value2",
                                    previousValue = null,
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_METADATA_KEY_2)
                                )
                            )
                            true
                        }
                        .expectNextMatches {
                            it.type shouldBe EventType.DELETED
                            it.elements shouldBe listOf(
                                Element(
                                    name = "key1",
                                    updatedValue = null,
                                    previousValue = "value1",
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_METADATA_KEY_1)
                                )
                            )
                            true
                        }
                        .expectNoEvent(noEventDuration)
                        .thenCancel()
                        .verify()
                }
            }

            When("Updating an Integer") {
                auditor.audit(
                    oldObject = getItem()
                        .copy(model = 1),
                    newObject = getItem()
                        .copy(model = 2)
                )
                Then("Assert that an Update Audit Event is sent for the model number") {
                    val consumedData = consumer
                        .map {
                            FunctionalTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java)
                        }
                    StepVerifier
                        .create(consumedData)
                        .expectNextMatches {
                            it.type shouldBe EventType.UPDATED
                            it.elements shouldBe listOf(
                                Element(
                                    name = "model",
                                    updatedValue = "2",
                                    previousValue = "1",
                                    metadata = ElementMetadata(fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_MODEL)
                                )
                            )
                            true
                        }
                        .expectNoEvent(noEventDuration)
                        .thenCancel()
                        .verify()
                }
            }

            When("Updating an Integer and static metadata is set using AuditorEventConfig ") {
                auditor.audit(
                    oldObject = getItem()
                        .copy(model = 1),
                    newObject = getItem()
                        .copy(model = 2),
                    auditorEventConfig = AuditorEventConfig(
                        applicationName = OVERRIDE_APPLICATION_NAME,
                        eventSource = EventSourceConfig(EventSourceType.USER),
                        eventSubType = "subType",
                        metadata = mapOf("itemNumber" to "some_static_itemNumber")
                    )
                )
                Then("Assert that an Update Audit Event is sent") {
                    val consumedData = consumer
                        .map {
                            FunctionalTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java)
                        }
                    StepVerifier
                        .create(consumedData)
                        .expectNextMatches {
                            it.type shouldBe EventType.UPDATED
                            it.metadata shouldBe mapOf("itemNumber" to "some_static_itemNumber")
                            it.elements shouldBe listOf(
                                Element(
                                    name = "model",
                                    updatedValue = "2",
                                    previousValue = "1",
                                    metadata = ElementMetadata(
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_MODEL
                                    )
                                )
                            )
                            true
                        }
                        .expectNoEvent(noEventDuration)
                        .thenCancel()
                        .verify()
                }
            }

            When("Updating an Integer and dynamic metadata is set using AuditorEventConfig ") {
                auditor.audit(
                    oldObject = getItem("14")
                        .copy(model = 1),
                    newObject = getItem("14")
                        .copy(model = 2),
                    auditorEventConfig = AuditorEventConfig(
                        applicationName = OVERRIDE_APPLICATION_NAME,
                        eventSource = EventSourceConfig(EventSourceType.USER),
                        eventSubType = "subType",
                        metadata = mapOf("itemNumber" to "\${itemNumber}-\${model}")
                    )
                )
                Then("Assert that an Update Audit Event is sent") {
                    val consumedData = consumer
                        .map {
                            FunctionalTestModule.objectMapper.readValue(it.value(), AuditEvent::class.java)
                        }
                    StepVerifier
                        .create(consumedData)
                        .expectNextMatches {
                            it.type shouldBe EventType.UPDATED
                            it.metadata shouldBe mapOf("itemNumber" to "14-2")
                            it.elements shouldBe listOf(
                                Element(
                                    name = "model",
                                    updatedValue = "2",
                                    previousValue = "1",
                                    metadata = ElementMetadata(
                                        fqdn = COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_MODEL
                                    )
                                )
                            )
                            true
                        }
                        .expectNoEvent(noEventDuration)
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
                        .expectNoEvent(noEventDuration)
                        .thenCancel()
                        .verify()
                }
            }
        }
    }

    private fun getItem(itemNumber: String? = "123"): Item {
        return Item(
            itemNumber = itemNumber,
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

/**
 * Constants used for the test cases
 */
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_ITEM_NUMBER = "com.lowes.auditor.client.api.model.Item.itemNumber"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_RAND_ID = "com.lowes.auditor.client.api.model.Item.rand.id"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_RAND_2_ID = "com.lowes.auditor.client.api.model.Item.rand2.id"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_LIST_ITEM_0_ID = "com.lowes.auditor.client.api.model.Item.listItem.0.id"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_LIST_ITEM_1_ID = "com.lowes.auditor.client.api.model.Item.listItem.1.id"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_MODEL = "com.lowes.auditor.client.api.model.Item.model"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_METADATA_KEY_1 = "com.lowes.auditor.client.api.model.Item.metadata.key1"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_ITEM_METADATA_KEY_2 = "com.lowes.auditor.client.api.model.Item.metadata.key2"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_EMAIL_ADDRESS = "com.lowes.auditor.client.api.model.User.emailAddress"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_FIRST_NAME = "com.lowes.auditor.client.api.model.User.firstName"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_LAST_NAME = "com.lowes.auditor.client.api.model.User.lastName"
private const val COM_LOWES_AUDITOR_CLIENT_API_MODEL_USER_NATIONALITY = "com.lowes.auditor.client.api.model.User.nationality"
private const val OVERRIDE_APPLICATION_NAME = "override-application-name"
