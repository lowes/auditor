package com.lowes.auditor.client.infrastructure.frameworks.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.infrastructure.frameworks.config.FrameworkModule
import com.lowes.auditor.client.infrastructure.frameworks.model.DummyClass
import com.lowes.auditor.client.infrastructure.frameworks.model.Item
import com.lowes.auditor.client.infrastructure.frameworks.model.Rand
import com.lowes.auditor.client.infrastructure.frameworks.model.SubObject
import com.lowes.auditor.core.entities.domain.Element
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

/**
 * Unit Tests for [ObjectDiffCheckerService]
 */
class ObjectDiffCheckerServiceTest : BehaviorSpec({
    val diffChecker = FrameworkModule.getObjectDiffChecker(AuditorEventConfig())
    val obj = ObjectMapper()

    Given("A objectDiffCheckerInstance and two objects are present") {
        val oldObject = DummyClass("123")
        val newObject = DummyClass("456")
        val startTime = System.currentTimeMillis()
        When("two objects are passed to diff checker module") {
            val diff = diffChecker.diff(oldObject, newObject).collectList().block()
            val endTime = System.currentTimeMillis()
            Then("diff time taken should be less than 90 ms") {
                endTime - startTime shouldBeLessThan 90
            }
            Then("diff list should not be empty") {
                diff.isNullOrEmpty() shouldBe false
            }
        }
    }

    Given("Old and New simple item Objects") {
        val oldItem = Item(
            itemNumber = "123",
            model = 1234,
            description = "old_item",
            price = BigDecimal.valueOf(1.23)
        )

        val newItem = Item(
            itemNumber = "1234",
            model = 1234,
            description = "new_item",
            price = BigDecimal.valueOf(5.67)
        )
        When("Only new simple object is present - Create") {
            val diff = diffChecker.diff(null, oldItem).collectList().block()
            Then("Only updated values are populated - Simple object") {
                diff shouldBe obj.readValue(javaClass.getResource("/create.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Compare old and new simple object") {
            val diff = diffChecker.diff(oldItem, newItem).collectList().block()
            Then("Contains all update, create and delete Events - Simple object") {
                diff shouldBe obj.readValue(javaClass.getResource("/udpate.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Only old simple object is present - Delete") {
            val diff = diffChecker.diff(newItem, null).collectList().block()
            Then("Only previous values are populates - Simple object") {
                diff shouldBe obj.readValue(javaClass.getResource("/delete.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Both Simple Objects are the same") {
            val diff = diffChecker.diff(newItem, newItem).collectList().block()
            Then("Empty List - Simple object") {
                diff shouldBe emptyList()
            }
        }
    }

    Given("Test for Collection Nested Lists") {
        val oldItem = Item(
            listItem = mutableListOf(
                Rand(
                    "rand0",
                    "rand0Name",
                    doubleList = listOf(SubObject(value = "value1", uom = "Ft")),
                    listString = listOf("test", "test1")
                )
            ),
        )

        val newItem = Item(
            listItem = mutableListOf(
                Rand(
                    "rand0",
                    "rand0Name",
                    doubleList = listOf(SubObject(value = "rand0value", uom = "rand0Ft")),
                    listString = listOf("rand0test", "rand0test1")
                ),
                Rand(
                    "rand1",
                    "rand1rand21321",
                    doubleList = listOf(SubObject(value = "rand1value1", uom = "rand1Ft")),
                    listString = listOf("rand1test", "rand1test1")
                ),
                Rand(
                    "rand2",
                    "rand2",
                    doubleList = listOf(SubObject(value = "rand2value1", uom = "rand2Ft")),
                    listString = listOf("rand2test", "rand2test1")
                )
            )
        )
        When("Only new nested list object is present - Create") {
            val diff = diffChecker.diff(null, oldItem).collectList().block()
            Then("Only updated values are populates - Nested list object") {
                diff shouldBe obj.readValue(javaClass.getResource("/InnerlistCreate.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Compare old and new nested list object") {
            val diff = diffChecker.diff(oldItem, newItem).collectList().block()
            Then("Conatains all update, create and delete Events - Nested list object") {
                diff shouldBe obj.readValue(javaClass.getResource("/InnerlistUpdate.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Only old nested list object is present - Delete") {
            val diff = diffChecker.diff(newItem, null).collectList().block()
            Then("Only previous values are populates - Nested list object") {
                diff shouldBe obj.readValue(javaClass.getResource("/InnerlistDelete.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Both nested list object are same") {
            val diff = diffChecker.diff(newItem, newItem).collectList().block()
            Then("Empty List - Nested list object") {
                diff shouldBe emptyList()
            }
        }
    }

    Given("Test for Collection Lists") {
        val oldItem = Item(
            stringList = listOf("String"),
            subList = listOf(SubObject(value = "auditor", uom = "cm"))
        )

        val newItem = Item(
            stringList = listOf("String123"),
            subList = listOf(SubObject(value = "auditor", uom = "in"))

        )
        When("Only new collection list object is present - Create") {
            val diff = diffChecker.diff(null, oldItem).collectList().block()
            Then("Only updated values are populates - Collection list object") {
                diff shouldBe obj.readValue(javaClass.getResource("/listCreate.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Compare old and new collection list objects") {
            val diff = diffChecker.diff(oldItem, newItem).collectList().block()
            Then("Conatains all update, create and delete Events - Collection list object") {
                diff shouldBe obj.readValue(javaClass.getResource("/listUpdate.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Only old collection list object is present - Delete") {
            val diff = diffChecker.diff(newItem, null).collectList().block()
            Then("Only previous values are populates - Collection list object") {
                diff shouldBe obj.readValue(javaClass.getResource("/listdelete.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Both collection list objects are the same") {
            val diff = diffChecker.diff(newItem, newItem).collectList().block()
            Then("Empty List - Collection list object") {
                diff shouldBe emptyList()
            }
        }
    }

    Given("Test for Collection map") {
        val oldItem = Item(
            subMap = mapOf("one" to SubObject("100", "ft")),
            metadata = mapOf("id" to "123")
        )

        val newItem = Item(
            subMap = mapOf("one" to SubObject("100", "ft")),
            metadata = mapOf("id" to "John")
        )
        When("Only new collection map object is present - Create") {
            val diff = diffChecker.diff(null, oldItem).collectList().block()
            Then("Only updated values are populates - Collection map object") {
                diff shouldBe obj.readValue(javaClass.getResource("/mapCreate.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When(" - Collection map objectCompare old and new collection map objects") {
            val diff = diffChecker.diff(oldItem, newItem).collectList().block()
            Then("Contains all update, create and delete Events - Collection map object") {
                diff shouldBe obj.readValue(javaClass.getResource("/mapUpdate.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Only old collection map object is present - Delete") {
            val diff = diffChecker.diff(newItem, null).collectList().block()
            Then("Only previous values are populates - Collection map object") {
                diff shouldBe obj.readValue(javaClass.getResource("/mapDelete.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Both collection map objects are the same") {
            val diff = diffChecker.diff(newItem, newItem).collectList().block()
            Then("Empty List - Collection map object") {
                diff shouldBe emptyList()
            }
        }
    }

    Given("Test for Collection mapInner") {
        val oldItem = Item(
            metadataRand = mapOf(
                "new_item_id" to Rand(
                    mapList = mapOf("list_007" to listOf(SubObject(value = "randMetamap007", uom = "randMetain007")))
                )
            )
        )

        val newItem = Item(
            metadataRand = mapOf(
                "new_item_id" to Rand(
                    mapList = mapOf("list" to listOf(SubObject(value = "randMetamap", uom = "randMetain")))
                )
            )
        )
        When("Only new collection mapInner object is present - Create") {
            val diff = diffChecker.diff(null, oldItem).collectList().block()
            Then("Only updated values are populates - Collection mapInner object") {
                diff shouldBe obj.readValue(javaClass.getResource("/mapInnerCreate.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Compare old and new collection mapInner objects") {
            val diff = diffChecker.diff(oldItem, newItem).collectList().block()
            Then("Contains all update, create and delete Events - Collection mapInner object") {
                diff shouldBe obj.readValue(javaClass.getResource("/mapInnerpUpdate.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Only old collection mapInner object is present - Delete") {
            val diff = diffChecker.diff(newItem, null).collectList().block()
            Then("Only previous values are populates - Collection mapInner object") {
                diff shouldBe obj.readValue(javaClass.getResource("/mapInnerDelete.json").readBytes(), Array<Element>::class.java).toList()
            }
        }

        When("Both collection mapInner objects are the same") {
            val diff = diffChecker.diff(newItem, newItem).collectList().block()
            Then("Empty List - Collection mapInner object") {
                diff shouldBe emptyList()
            }
        }
    }
})
