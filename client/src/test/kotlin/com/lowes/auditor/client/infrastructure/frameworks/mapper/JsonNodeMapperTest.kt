package com.lowes.auditor.client.infrastructure.frameworks.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.ElementMetadata
import com.lowes.auditor.core.entities.domain.EventType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith

/**
 * Unit Tests for [JsonNodeMapper]
 */
class JsonNodeMapperTest : BehaviorSpec({
    val objectMapper = ObjectMapper()
    val jsonNodeFactory = JsonNodeFactory.instance

    Given("A JsonNodeMapper and a simple JSON object") {
        val simpleJson =
            """
            {
                "id": 1,
                "name": "Test",
                "active": true
            }
            """.trimIndent()

        val jsonNode = objectMapper.readTree(simpleJson)
        val fqcn = "com.example.Test"

        When("converting to elements with CREATED event type") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should create elements with correct FQDNs and values") {
                elements?.size shouldBe 3

                val expectedElements =
                    listOf(
                        Element(
                            name = "id",
                            updatedValue = "1",
                            previousValue = null,
                            metadata = ElementMetadata(fqdn = "$fqcn.id"),
                        ),
                        Element(
                            name = "name",
                            updatedValue = "Test",
                            previousValue = null,
                            metadata = ElementMetadata(fqdn = "$fqcn.name"),
                        ),
                        Element(
                            name = "active",
                            updatedValue = "true",
                            previousValue = null,
                            metadata = ElementMetadata(fqdn = "$fqcn.active"),
                        ),
                    )

                elements?.forEach { element ->
                    expectedElements.first { it.name == element.name }.let { expected ->
                        element.updatedValue shouldBe expected.updatedValue
                        element.metadata?.fqdn shouldBe expected.metadata?.fqdn
                    }
                }
            }
        }

        When("converting to elements with DELETED event type") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.DELETED, fqcn)
                    .collectList()
                    .block()

            Then("should set previousValue instead of updatedValue") {
                elements?.forEach { element ->
                    element.updatedValue shouldBe null
                    element.previousValue shouldNotBe null
                }
            }
        }
    }

    Given("A JsonNodeMapper and a nested JSON object") {
        val nestedJson =
            """
            {
                "id": 1,
                "name": "Test",
                "address": {
                    "street": "123 Main St",
                    "city": "Anytown",
                    "zip": "12345"
                }
            }
            """.trimIndent()

        val jsonNode = objectMapper.readTree(nestedJson)
        val fqcn = "com.example.User"

        When("converting to elements") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should handle nested objects correctly") {
                elements?.size shouldBe 5 // id, name, address.street, address.city, address.zip

                val addressElements = elements?.filter { it.name == "street" || it.name == "city" || it.name == "zip" }
                addressElements?.size shouldBe 3

                addressElements?.forEach { element ->
                    element.metadata?.fqdn shouldStartWith "$fqcn.address"
                }
            }
        }
    }

    Given("A JsonNodeMapper and a JSON array") {
        val arrayJson =
            """
            [
                {"id": 1, "name": "Item 1"},
                {"id": 2, "name": "Item 2"}
            ]
            """.trimIndent()

        val jsonNode = objectMapper.readTree(arrayJson)
        val fqcn = "com.example.Items"

        When("converting array to elements") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should create elements with array indices in FQDN") {
                elements?.size shouldBe 4 // 2 items * 2 fields each
                println(elements?.toString())
                elements?.find { it.name == "id" && it.metadata?.fqdn == "$fqcn.0.id" }?.updatedValue shouldBe "1"
                elements?.find { it.name == "name" && it.metadata?.fqdn == "$fqcn.0.name" }?.updatedValue shouldBe "Item 1"
                elements?.find { it.name == "id" && it.metadata?.fqdn == "$fqcn.1.id" }?.updatedValue shouldBe "2"
                elements?.find { it.name == "name" && it.metadata?.fqdn == "$fqcn.1.name" }?.updatedValue shouldBe "Item 2"
            }
        }
    }

    Given("A JsonNodeMapper and a complex nested JSON object") {
        val complexJson =
            """
            {
                "id": 1,
                "name": "Test User",
                "addresses": [
                    {
                        "type": "home",
                        "street": "123 Main St",
                        "city": "Anytown"
                    },
                    {
                        "type": "work",
                        "street": "456 Business Ave",
                        "city": "Businesstown"
                    }
                ],
                "preferences": {
                    "notifications": true,
                    "theme": "dark",
                    "favoriteCategories": ["tech", "books", "music"]
                }
            }
            """.trimIndent()

        val jsonNode = objectMapper.readTree(complexJson)
        val fqcn = "com.example.UserProfile"

        When("converting complex object to elements") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should handle all nested structures correctly") {
                // Top level fields
                elements?.find { it.metadata?.fqdn == "$fqcn.id" }?.updatedValue shouldBe "1"
                elements?.find { it.metadata?.fqdn == "$fqcn.name" }?.updatedValue shouldBe "Test User"

                // Nested array of objects (addresses)
                elements?.find { it.metadata?.fqdn == "$fqcn.addresses.0.type" }?.updatedValue shouldBe "home"
                elements?.find { it.metadata?.fqdn == "$fqcn.addresses.0.street" }?.updatedValue shouldBe "123 Main St"
                elements?.find { it.metadata?.fqdn == "$fqcn.addresses.1.type" }?.updatedValue shouldBe "work"
                elements?.find { it.metadata?.fqdn == "$fqcn.addresses.1.city" }?.updatedValue shouldBe "Businesstown"

                // Nested object with array (preferences.favoriteCategories)
                elements?.find { it.metadata?.fqdn == "$fqcn.preferences.notifications" }?.updatedValue shouldBe "true"
                elements?.find { it.metadata?.fqdn == "$fqcn.preferences.theme" }?.updatedValue shouldBe "dark"

                // Array handling within nested object
                val categoryElements = elements?.filter { it.name == "favoriteCategories" }
                categoryElements?.size shouldBe 3
                categoryElements?.map { it.updatedValue }?.toSet() shouldBe setOf("tech", "books", "music")
            }
        }
    }

    Given("A JsonNodeMapper and edge case JSON values") {
        val edgeCaseJson =
            """
            {
                "nullValue": null,
                "emptyString": "",
                "zero": 0,
                "falseValue": false,
                "emptyObject": {},
                "emptyArray": []
            }
            """.trimIndent()

        val jsonNode = objectMapper.readTree(edgeCaseJson)
        val fqcn = "com.example.EdgeCases"

        When("converting edge case values to elements") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should handle all edge cases correctly") {
                elements?.find { it.name == "nullValue" }?.updatedValue shouldBe "null"
                elements?.find { it.name == "emptyString" }?.updatedValue shouldBe ""
                elements?.find { it.name == "zero" }?.updatedValue shouldBe "0"
                elements?.find { it.name == "falseValue" }?.updatedValue shouldBe "false"
                elements?.find { it.name == "emptyObject" } shouldBe null // Empty objects should be filtered out
                elements?.find { it.name == "emptyArray" } shouldBe null // Empty arrays should be filtered out
            }
        }
    }

    Given("A JsonNodeMapper and a programmatically created complex object") {
        val rootNode: ObjectNode = jsonNodeFactory.objectNode()
        val addressArray: ArrayNode = jsonNodeFactory.arrayNode()

        // Create address 1
        val address1 = jsonNodeFactory.objectNode()
        address1.put("type", "home")
        address1.put("street", "123 Main St")
        address1.put("city", "Anytown")

        // Create address 2
        val address2 = jsonNodeFactory.objectNode()
        address2.put("type", "work")
        address2.put("street", "456 Business Ave")
        address2.put("city", "Businesstown")

        // Add addresses to array
        addressArray.add(address1)
        addressArray.add(address2)

        // Create preferences
        val preferences = jsonNodeFactory.objectNode()
        preferences.put("notifications", true)
        preferences.put("theme", "dark")

        val favoriteCategories = jsonNodeFactory.arrayNode()
        favoriteCategories.add("tech")
        favoriteCategories.add("books")
        favoriteCategories.add("music")
        preferences.set<ArrayNode>("favoriteCategories", favoriteCategories)

        // Build root object
        rootNode.put("id", 1)
        rootNode.put("name", "Test User")
        rootNode.set<ArrayNode>("addresses", addressArray)
        rootNode.set<ObjectNode>("preferences", preferences)

        val fqcn = "com.example.ProgrammaticUser"

        When("converting programmatically created complex object to elements") {
            val elements =
                JsonNodeMapper.toElement(rootNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should handle all programmatic structures correctly") {
                // Basic fields
                elements?.find { it.metadata?.fqdn == "$fqcn.id" }?.updatedValue shouldBe "1"
                elements?.find { it.metadata?.fqdn == "$fqcn.name" }?.updatedValue shouldBe "Test User"

                // Nested arrays and objects
                elements?.find { it.metadata?.fqdn == "$fqcn.addresses.0.type" }?.updatedValue shouldBe "home"
                elements?.find { it.metadata?.fqdn == "$fqcn.addresses.1.street" }?.updatedValue shouldBe "456 Business Ave"
                elements?.find { it.metadata?.fqdn == "$fqcn.preferences.theme" }?.updatedValue shouldBe "dark"

                // Array within nested object
                val categoryElements = elements?.filter { it.name == "favoriteCategories" }?.map { it.updatedValue }
                categoryElements shouldContainExactlyInAnyOrder listOf("tech", "books", "music")
            }
        }
    }
})
