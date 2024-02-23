package foo.model

import java.util.UUID

data class Item(
    val itemNumber: UUID,
    val model: Int,
    val description: String,
    val metadata: Map<String, String>?,
    val rand: Rand?,
    val rand2: Rand?,
    val listItem: MutableList<Rand> = mutableListOf(),
    val metadataRand: Map<String, Rand>? = mapOf(),
)

data class Rand(
    val id: String,
    val name: String,
)
