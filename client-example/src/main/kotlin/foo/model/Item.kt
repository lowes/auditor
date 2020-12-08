package foo.model

import java.util.UUID

data class Item(
    val itemNumber: UUID,
    val model: Int,
    val description: String,
    val metadata: Map<String, String>?,
)
