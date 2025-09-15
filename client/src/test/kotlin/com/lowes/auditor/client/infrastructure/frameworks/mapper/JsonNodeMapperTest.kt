package com.lowes.auditor.client.infrastructure.frameworks.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.core.entities.domain.Element
import com.lowes.auditor.core.entities.domain.EventType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Unit Tests for [JsonNodeMapper]
 */
class JsonNodeMapperTest : BehaviorSpec({
    val objectMapper = ObjectMapper()
    val fqcn = "com.example.Test"

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

        When("converting to elements with CREATED event type") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should create elements with correct FQDNs and values") {
                elements.shouldNotBeNull()

                val expectedElements: List<Element> =
                    objectMapper.readValue(
                        javaClass.getResource("/simpleCreated.json").readBytes(),
                        Array<Element>::class.java,
                    ).toList()

                // Sort both lists by name for consistent comparison
                val sortedActual = elements.sortedBy { it.name }
                val sortedExpected = expectedElements.sortedBy { it.name }

                sortedActual shouldBe sortedExpected
            }
        }

        When("converting to elements with DELETED event type") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.DELETED, fqcn)
                    .collectList()
                    .block()

            Then("should set previousValue instead of updatedValue") {
                elements.shouldNotBeNull()

                val expectedElements: List<Element> =
                    objectMapper.readValue(
                        javaClass.getResource("/simpleDeleted.json").readBytes(),
                        Array<Element>::class.java,
                    ).toList()

                // Sort both lists by name for consistent comparison
                val sortedActual = elements.sortedBy { it.name }
                val sortedExpected = expectedElements.sortedBy { it.name }

                sortedActual shouldBe sortedExpected
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

        When("converting to elements") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should handle nested objects correctly") {
                elements.shouldNotBeNull()

                val expectedElements: List<Element> =
                    objectMapper.readValue(
                        javaClass.getResource("/nestedCreated.json").readBytes(),
                        Array<Element>::class.java,
                    ).toList()

                // Sort both lists by name and fqdn for consistent comparison
                val sortedActual = elements.sortedWith(compareBy({ it.name }, { it.metadata?.fqdn }))
                val sortedExpected = expectedElements.sortedWith(compareBy({ it.name }, { it.metadata?.fqdn }))

                sortedActual shouldBe sortedExpected
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

        When("converting complex object to elements") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should handle all nested structures correctly") {
                elements.shouldNotBeNull()

                val expectedElements: List<Element> =
                    objectMapper.readValue(
                        javaClass.getResource("/complexCreated.json").readBytes(),
                        Array<Element>::class.java,
                    ).toList()

                // Sort both lists by fqdn for consistent comparison
                val sortedActual = elements.sortedBy { it.metadata?.fqdn }
                val sortedExpected = expectedElements.sortedBy { it.metadata?.fqdn }

                sortedActual shouldBe sortedExpected
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

        When("converting nested arrays to elements") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should handle all levels of nested arrays correctly") {
                elements.shouldNotBeNull()

                val expectedElements: List<Element> =
                    objectMapper.readValue(
                        javaClass.getResource("/nestedArrayCreated.json").readBytes(),
                        Array<Element>::class.java,
                    ).toList()

                // Sort both lists by fqdn for consistent comparison
                val sortedActual = elements.sortedBy { it.metadata?.fqdn }
                val sortedExpected = expectedElements.sortedBy { it.metadata?.fqdn }

                sortedActual shouldBe sortedExpected
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

        When("converting edge case values to elements") {
            val elements =
                JsonNodeMapper.toElement(jsonNode, EventType.CREATED, fqcn)
                    .collectList()
                    .block()

            Then("should handle all edge cases correctly") {
                elements.shouldNotBeNull()

                val expectedElements: List<Element> =
                    objectMapper.readValue(
                        javaClass.getResource("/edgeCaseCreated.json").readBytes(),
                        Array<Element>::class.java,
                    ).toList()

                // Sort both lists by name for consistent comparison
                val sortedActual = elements.sortedBy { it.name }
                val sortedExpected = expectedElements.sortedBy { it.name }

                sortedActual shouldBe sortedExpected

                // Explicitly verify empty objects and arrays are filtered out
                elements.find { it.name == "emptyObject" } shouldBe null
                elements.find { it.name == "emptyArray" } shouldBe null
            }
        }
    }
})
