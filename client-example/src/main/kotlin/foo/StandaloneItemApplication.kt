package foo

import com.lowes.auditor.client.api.Auditor
import com.lowes.auditor.client.api.Auditor.Companion.getInstance
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.infrastructure.event.config.AuditEventProducerConfig
import foo.model.Item
import foo.model.Rand
import java.util.UUID

class StandaloneItemApplication(
    private val auditor: Auditor,
) : Runnable {

    companion object {
        const val ZERO = 0
        const val FIVE_THOUSAND = 5000
    }

    override fun run() {
        val itemNumber = UUID.randomUUID()
        val oldItem = Item(itemNumber = itemNumber, model = 1234, description = "old_item", rand2 = Rand("random2"))

        val newItem = Item(
            itemNumber = itemNumber,
            model = 9876,
            description = "new_item",
            metadata = mapOf("new_item_id" to "98767", "new_2" to "213"),
            rand = Rand("randID"),
            rand2 = null,
            metadataRand = mapOf("new_item_id" to Rand("98767"))
        )
        println("Running auditor! for newItemNumber $itemNumber")
        auditor.audit(oldItem, newItem, AuditorEventConfig(metadata = mapOf("itenNumber" to "\${itemNumber}")))
        var count = ZERO
        val bigObject = generateSequence { (count++).takeIf { count < FIVE_THOUSAND } }
            .map { ("BigValue_").plus(it) to it }.toMap()
        auditor.log(bigObject)
        println("Done")
    }
}

fun main() {
    val producerConfig = AuditEventProducerConfig(
        enabled = true,
        bootstrapServers = "localhost:9092",
        topic = "auditTopic",
        configs = mapOf("client.id" to "client-example")
    )
    val auditorEventConfig = AuditorEventConfig(
        applicationName = "client-example"
    )
    val auditor = getInstance(producerConfig, auditorEventConfig)
    StandaloneItemApplication(auditor).run()
}
