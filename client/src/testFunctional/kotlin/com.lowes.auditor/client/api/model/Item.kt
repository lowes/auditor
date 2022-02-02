package com.lowes.auditor.client.api.model

import java.math.BigDecimal

/**
 * Item Data Class created for test purposes
 */
data class Item(
    val itemNumber: String? = null,
    val model: Int? = null,
    val description: String? = null,
    val metadata: Map<String, String>? = null,
    val rand: Rand? = null,
    val rand2: Rand? = null,
    val listItem: MutableList<Rand> ? = null,
    val metadataRand: Map<String, Rand>? = null,
    val stringList: List<String>? = null,
    val price: BigDecimal? = null,
    val subList: List<SubObject>? = null,
    val subMap: Map<String, SubObject>? = null
)

data class Rand(
    val id: String? = null,
    val name: String? = null,
    val doubleList: List<SubObject>? = null,
    val listString: List<String>? = null,
    val mapList: Map<String, List<SubObject>>? = null
)

data class SubObject(
    val value: String? = null,
    val uom: String? = null
)
