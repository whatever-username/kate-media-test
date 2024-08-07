package com.whatever.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable

object Counter : LongIdTable() {
    val name = varchar("name", 255).uniqueIndex()
    val value = integer("value")
}

@Serializable
data class CounterDTO(
    val name: String,
    var value: Int
)