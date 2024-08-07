package com.whatever.repository

import com.whatever.model.Counter
import com.whatever.model.CounterDTO
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.updateReturning
import java.sql.Connection

class PostgresCounterRepository {

    suspend fun createCounter(name: String, value: Int) {
        tz {
            Counter.insert {
                it[Counter.name] = name
                it[Counter.value] = value
            }
        }
    }

    suspend fun getCounter(name: String): Int? {
        return tz {
            Counter.select(Counter.value)
                .where(Counter.name eq name)
                .map { it[Counter.value] }
                .singleOrNull()

        }
    }

    suspend fun deleteCounter(name: String): Boolean {
        return tz {
            Counter.deleteWhere { Counter.name eq name } > 0
        }
    }

    suspend fun incrementCounter(name: String): Int {
        return tz {
            Counter.updateReturning(
                where = { Counter.name eq name },
                returning = listOf(Counter.value)
            ) {
                it[value] = value + 1
            }.singleOrNull()?.let { it[Counter.value] }
                ?: throw IllegalArgumentException("Counter with name $name not found")
        }
    }

    suspend fun getAllCounters(): List<CounterDTO> {
        return tz {
            Counter.selectAll().map { it.toCounterDTO() }
        }
    }

    private fun ResultRow.toCounterDTO(): CounterDTO {
        return CounterDTO(
            name = this[Counter.name],
            value = this[Counter.value]
        )
    }

    private suspend fun <T> tz(operation: () -> T): T {
        return newSuspendedTransaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
            operation()
        }
    }
}


