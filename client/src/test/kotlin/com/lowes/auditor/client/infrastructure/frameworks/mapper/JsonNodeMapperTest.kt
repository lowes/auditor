package com.lowes.auditor.client.infrastructure.frameworks.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.ElementMetadata
import com.lowes.auditor.core.entities.domain.EventType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith

/**
 * Unit Tests for [JsonNodeMapper]
 */
class JsonNodeMapperTest : BehaviorSpec({
    val objectMapper = ObjectMapper()

    Given("a simple JSON object with primitive values") {
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

    Given("a JSON object with nested objects") {
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

    Given("a complex JSON object with arrays and nested objects") {
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
                // Verify top-level primitive fields are correctly mapped
                elements?.find { it.metadata?.fqdn == "$fqcn.id" }?.updatedValue shouldBe "1"
                elements?.find { it.metadata?.fqdn == "$fqcn.name" }?.updatedValue shouldBe "Test User"

                // Verify nested array of objects with proper FQDN construction
                elements?.find { it.metadata?.fqdn == "$fqcn.addresses.0.type" }?.updatedValue shouldBe "home"
                elements?.find { it.metadata?.fqdn == "$fqcn.addresses.0.street" }?.updatedValue shouldBe "123 Main St"
                elements?.find { it.metadata?.fqdn == "$fqcn.addresses.1.type" }?.updatedValue shouldBe "work"
                elements?.find { it.metadata?.fqdn == "$fqcn.addresses.1.city" }?.updatedValue shouldBe "Businesstown"

                // Verify nested object with array field
                elements?.find { it.metadata?.fqdn == "$fqcn.preferences.notifications" }?.updatedValue shouldBe "true"
                elements?.find { it.metadata?.fqdn == "$fqcn.preferences.theme" }?.updatedValue shouldBe "dark"

                // Verify array values within nested object are correctly flattened
                val categoryElements = elements?.filter { it.name == "favoriteCategories" }
                categoryElements?.size shouldBe 3
                categoryElements?.map { it.updatedValue }?.toSet() shouldBe setOf("tech", "books", "music")
            }
        }
    }

    Given("a JSON object with various array structures") {
        val nestedArrayJson =
            """
            {
                "matrix": [
                    [1, 2, 3],
                    [4, 5, 6],
                    [7, 8, 9]
                ],
                "nested": {
                    "deepArray": [
                        [
                            {"id": 1, "values": ["a", "b"]},
                            {"id": 2, "values": ["c", "d"]}
                        ],
                        [
                            {"id": 3, "values": ["e", "f"]}
                        ]
                    ]
                },
                "simpleArray": [
                    {"id": 1, "name": "Item 1"},
                    {"id": 2, "name": "Item 2"}
                ]
            }
            """.trimIndent()

        val jsonNode = objectMapper.readTree(nestedArrayJson)
        val fqcn = "com.example.NestedArrays"

        When("converting nested arrays to elements") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should handle all levels of nested arrays correctly") {
                elements.shouldNotBeNull()

                // Verify 2D array access with proper indices
                elements.find { it.metadata?.fqdn == "$fqcn.matrix.0.0" }?.updatedValue shouldBe "1"
                elements.find { it.metadata?.fqdn == "$fqcn.matrix.1.2" }?.updatedValue shouldBe "6"
                elements.find { it.metadata?.fqdn == "$fqcn.matrix.2.1" }?.updatedValue shouldBe "8"

                // Verify deeply nested arrays with objects and proper FQDN construction
                elements.find { it.metadata?.fqdn == "$fqcn.nested.deepArray.0.0.id" }?.updatedValue shouldBe "1"
                elements.find { it.metadata?.fqdn == "$fqcn.nested.deepArray.0.1.values.1" }?.updatedValue shouldBe "d"
                elements.find { it.metadata?.fqdn == "$fqcn.nested.deepArray.1.0.values.0" }?.updatedValue shouldBe "e"

                // Verify simple array of objects with proper FQDN construction
                elements.find { it.metadata?.fqdn == "$fqcn.simpleArray.0.id" }?.updatedValue shouldBe "1"
                elements.find { it.metadata?.fqdn == "$fqcn.simpleArray.0.name" }?.updatedValue shouldBe "Item 1"
                elements.find { it.metadata?.fqdn == "$fqcn.simpleArray.1.id" }?.updatedValue shouldBe "2"
                elements.find { it.metadata?.fqdn == "$fqcn.simpleArray.1.name" }?.updatedValue shouldBe "Item 2"
            }
        }
    }

    Given("a JSON object with edge case values") {
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
                // Verify empty objects and arrays are filtered out
                elements?.find { it.name == "emptyObject" } shouldBe null
                elements?.find { it.name == "emptyArray" } shouldBe null
            }
        }
    }
})
