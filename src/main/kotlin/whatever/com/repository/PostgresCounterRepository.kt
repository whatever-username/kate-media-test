package whatever.com.repository

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import whatever.com.model.Counter
import whatever.com.model.CounterExposed


class PostgresCounterRepository(
) : CounterRepository {

    override suspend fun createCounter(name: String, value: Int) {
        transaction {
            CounterExposed.insert {
                it[CounterExposed.name] = name
                it[CounterExposed.value] = value
            }
        }
    }

    override suspend fun getCounter(name: String): Counter? {
        return transaction {
            CounterExposed.select { CounterExposed.name eq name }
                .map { toCounter(it) }
                .singleOrNull()
        }
    }

    override suspend fun deleteCounter(name: String): Boolean {
        return transaction {
            CounterExposed.deleteWhere { CounterExposed.name eq name } > 0
        }
    }

    override suspend fun incrementCounter(name: String): Int {
        return transaction {
            CounterExposed.update({ CounterExposed.name eq name }) {
                with(SqlExpressionBuilder) {
                    it.update(CounterExposed.value, CounterExposed.value + 1)
                }
            }
            CounterExposed.select { CounterExposed.name eq name }
                .map { it[CounterExposed.value] }
                .singleOrNull() ?: throw IllegalStateException("Counter not found")
        }
    }

    override suspend fun getAllCounters(): List<Counter> {
        return transaction {
            CounterExposed.selectAll().map { toCounter(it) }
        }
    }

    private fun toCounter(row: ResultRow): Counter {
        return Counter(
            id = row[CounterExposed.id].value,
            name = row[CounterExposed.name],
            value = row[CounterExposed.value]
        )
    }
}