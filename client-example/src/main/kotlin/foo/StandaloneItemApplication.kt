package foo

import com.lowes.auditor.client.api.Auditor
import com.lowes.auditor.client.api.Auditor.Companion.getInstance
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.ElementFilter
import com.lowes.auditor.client.entities.domain.ElementFilterOptions
import com.lowes.auditor.client.entities.domain.EventFilter
import com.lowes.auditor.client.entities.domain.EventSourceConfig
import com.lowes.auditor.client.entities.domain.EventSourceMetadataConfig
import com.lowes.auditor.client.entities.domain.Filters
import com.lowes.auditor.client.entities.domain.LoggingFilter
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import com.lowes.auditor.core.entities.domain.EventSourceType
import com.lowes.auditor.core.entities.domain.EventType
import foo.model.Item
import foo.model.ItemData
import java.util.UUID

class StandaloneItemApplication(
    private val auditor: Auditor,
) : Runnable {
    private fun testCreateOldObjectNullAndNewObject(itemNumber: UUID) {
        val oldItem = null
        val newItem = getItem(itemNumber)
        auditor.audit(oldItem, newItem)
    }

    private fun testDeleteOldObjectNotNullAndNewObjectAsNull(itemNumber: UUID) {
        val oldItem = getItem(itemNumber)
        val newItem = null
        auditor.audit(oldItem, newItem)
    }

    private fun testUpdateBothObject(itemNumber: UUID) {
        val oldItem = getItem(itemNumber)
        val newItem =
            getItem(itemNumber)
                .copy(
                    description = "new_item_description",
                    model = 100,
                    metadata = mapOf("MetaKey" to "MetaValue", "NewMetaKey" to "NewMetaValue"),
                    categories = emptyList(),
                    data = ItemData(""),
                )
        auditor.audit(oldItem, newItem)
    }

    private fun testStaticDataSubstitution(itemNumber: UUID) {
        val oldItem = getItem(itemNumber)
        val newItem =
            getItem(itemNumber)
                .copy(description = "new_item_description")
        val config =
            AuditorEventConfig(
                applicationName = "client-example-kotlin",
                eventSource = EventSourceConfig(EventSourceType.USER, EventSourceMetadataConfig("static-user-id")),
                metadata = mapOf("itemNumber" to "Sadly i am only static-value"),
            )
        auditor.audit(oldItem, newItem, config)
    }

    private fun testDynamicDataSubstitution(itemNumber: UUID) {
        val oldItem = getItem(itemNumber)
        val newItem =
            getItem(itemNumber)
                .copy(description = "new_item_description", data = ItemData("NewItemDataValue"))
        val config =
            AuditorEventConfig(
                applicationName = "client-example-kotlin",
                eventSource = EventSourceConfig(EventSourceType.USER, EventSourceMetadataConfig("\${updatedBy}")),
                metadata =
                    mapOf(
                        "itemNumber" to "\${itemNumber}",
                        "price.data" to "\${data.value}",
                        "static-key" to "static-value",
                    ),
            )
        auditor.audit(oldItem, newItem, config)
    }

    private fun testEventFilters(itemNumber: UUID) {
        val oldItem =
            getItem(itemNumber)
                .copy(categories = null)
        val newItem =
            getItem(itemNumber)
                .copy(description = "new_item_description", model = 100)
        val config =
            AuditorEventConfig(
                filters = Filters(event = EventFilter(enabled = true, type = listOf(EventType.CREATED, EventType.UPDATED))),
            )
        auditor.audit(oldItem, newItem, config)
    }

    private fun testElementIncludesFilters(itemNumber: UUID) {
        val oldItem = getItem(itemNumber)
        val newItem =
            getItem(itemNumber)
                .copy(description = "new_item_description", model = 100)
        val config =
            AuditorEventConfig(
                filters =
                    Filters(
                        element =
                            ElementFilter(
                                enabled = true,
                                listOf("InclusionFilter"),
                                options = ElementFilterOptions(includes = listOf("description")),
                            ),
                    ),
            )
        auditor.audit(oldItem, newItem, config)
    }

    private fun testElementExcludesFilters(itemNumber: UUID) {
        val oldItem = getItem(itemNumber)
        val newItem = getItem(itemNumber).copy(description = "new_item_description", model = 100)
        val config =
            AuditorEventConfig(
                filters =
                    Filters(
                        element =
                            ElementFilter(
                                enabled = true,
                                listOf("ExclusionFilter"),
                                options = ElementFilterOptions(excludes = listOf("description")),
                            ),
                    ),
            )
        auditor.audit(oldItem, newItem, config)
    }

    private fun testLoggingFilters(itemNumber: UUID) {
        val oldItem = getItem(itemNumber)
        val newItem =
            getItem(itemNumber)
                .copy(description = "new_item_description")
        val config =
            AuditorEventConfig(
                filters = Filters(logging = LoggingFilter(true)),
            )
        auditor.audit(oldItem, newItem, config)
    }

    private fun testLog(itemNumber: UUID) {
        auditor.log(getItem(itemNumber))
    }

    private fun getItem(itemNumber: UUID): Item {
        return Item(
            itemNumber = itemNumber,
            model = 1234,
            description = "old_item_description",
            metadata = mapOf("MetaKey" to "metaValue"),
            categories = listOf("categories"),
            data = ItemData("ItemDataValue"),
            updatedBy = "DoctorStrange!",
        )
    }

    override fun run() {
        val itemNumber = UUID.randomUUID()
        println("Running auditor! for$itemNumber")
        testCreateOldObjectNullAndNewObject(itemNumber)
        testDeleteOldObjectNotNullAndNewObjectAsNull(itemNumber)
        testUpdateBothObject(itemNumber)
        testStaticDataSubstitution(itemNumber)
        testDynamicDataSubstitution(itemNumber)
        testEventFilters(itemNumber)
        testElementIncludesFilters(itemNumber)
        testElementExcludesFilters(itemNumber)
        testLoggingFilters(itemNumber)
        testLog(itemNumber)
        println("Done")
    }
}

fun main() {
    val producerConfig =
        AuditEventProducerConfig(
            enabled = true,
            bootstrapServers = "localhost:9092",
            topic = "auditTopic",
            configs = mapOf("client.id" to "client-example"),
        )
    val auditorEventConfig = AuditorEventConfig(applicationName = "client-example")
    val auditor = getInstance(producerConfig, auditorEventConfig)
    StandaloneItemApplication(auditor).run()
}
