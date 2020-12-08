package foo

import com.lowes.auditor.client.api.Auditor
import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.domain.EventSource
import com.lowes.auditor.client.entities.domain.EventSourceMetadata
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
        auditor.audit(
            oldItem,
            newItem,
            AuditorEventConfig(
                eventSource = EventSource.USER,
                eventSourceMetadata = EventSourceMetadata(
                    id = "User-Id-${UUID.randomUUID()}"
                )
            )
        )
        println("Done")
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
