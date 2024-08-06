package whatever.com.repository

import whatever.com.model.Counter

interface CounterRepository {

    suspend fun createCounter(name: String, value: Int)
    suspend fun getCounter(name: String): Counter?
    suspend fun deleteCounter(name: String): Boolean
    suspend fun incrementCounter(name: String): Int
    suspend fun getAllCounters(): List<Counter>
}