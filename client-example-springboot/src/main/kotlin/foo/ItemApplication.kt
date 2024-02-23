package foo

import com.lowes.auditor.client.api.Auditor
import foo.model.Item
import foo.model.Rand
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.UUID

@EnableAutoConfiguration
@SpringBootApplication
class ItemApplication(
    val auditor: Auditor,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val oldItem =
            Item(
                itemNumber = UUID.randomUUID(),
                model = 1234,
                description = "old_item",
                metadata = null,
                rand = null,
                rand2 = null,
                listItem = mutableListOf(Rand("21321", "rand21321")),
            )

        val newItemNumber = UUID.randomUUID()
        val newItem =
            Item(
                itemNumber = newItemNumber,
                model = 9876,
                description = "new_item",
                metadata = mapOf("new_item_id" to "98767", "new_2" to "213"),
                rand = Rand("randID", "randID123"),
                rand2 = null,
                listItem = mutableListOf(Rand("12easx", "r12easx"), Rand("21321", "rand21321"), Rand("asdnjj", "rasdnjj")),
                metadataRand = mapOf("new_item_id" to Rand("98767", "r98767")),
            )
        println("Running auditor! for newItemNumber $newItemNumber")
        auditor.audit(
            null,
            newItem,
        )

        auditor.audit(
            newItem,
            null,
        )

        auditor.audit(
            oldItem,
            newItem,
        )

        auditor.log(newItem.copy(description = "logging description"))

        println("Done")
    }
}

fun main(args: Array<String>) {
    runApplication<ItemApplication>(*args)
}
