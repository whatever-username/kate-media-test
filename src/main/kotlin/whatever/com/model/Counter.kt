package whatever.com.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable

object CounterExposed : LongIdTable() {
    val name = varchar("name", 255).uniqueIndex()
    val value = integer("value")
}
@Serializable
data class Counter(
    val id: Long? = null,
    val name: String,
    var value: Int
)