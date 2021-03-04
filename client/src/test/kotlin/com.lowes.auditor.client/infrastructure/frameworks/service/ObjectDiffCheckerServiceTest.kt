package com.lowes.auditor.client.infrastructure.frameworks.service

import com.lowes.auditor.client.infrastructure.frameworks.config.ObjectDiffModule
import com.lowes.auditor.client.infrastructure.frameworks.model.DummyClass
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe

internal class ObjectDiffCheckerServiceTest : BehaviorSpec({
    Given("A objectDiffCheckerInstance and two objects are present") {
        val diffChecker = ObjectDiffModule.objectDiffChecker
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
})
