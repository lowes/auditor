package foo

import com.lowes.auditor.client.api.Auditor
import foo.model.Item
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.UUID

@SpringBootApplication
class Application(
    val auditor: Auditor
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val oldItem = Item(
            itemNumber = UUID.randomUUID(),
            model = 1234,
            description = "old_item",
            metadata = null
        )

        val newItemNumber = UUID.randomUUID()
        val newItem = Item(
            itemNumber = newItemNumber,
            model = 9876,
            description = "new_item",
            metadata = mapOf("new_item_id" to "98767")
        )
        println("Running auditor! for newItemNumber $newItemNumber")
        for (i in 1..1) {
            auditor.audit(oldItem, newItem)
        }
        println("Done")
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
