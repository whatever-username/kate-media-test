package com.whatever

import com.whatever.model.Counter
import com.whatever.model.CounterDTO
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.config.mergeWith
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals


class ApplicationTest {

    val database = Database()

    private fun ApplicationTestBuilder.createTestClient() = createClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json { prettyPrint = true; isLenient = true })
        }
    }

    private fun testApplicationCustom(block: suspend ApplicationTestBuilder.() -> Unit): Unit {
        return testApplication {
            environment {
                config = ApplicationConfig("test-application.yaml").mergeWith(
                    MapApplicationConfig("postgres.url" to database.postgresContainer.jdbcUrl)
                )
            }
            block()
        }
    }

    @AfterTest
    fun cleanup() {
        transaction { Counter.deleteAll() }
    }

    @Test
    fun testCreateCounter() = testApplicationCustom {
        val client = createTestClient()

        val response = client.post("/api/v1/counter") {
            contentType(ContentType.Application.Json)
            setBody(CounterDTO(name = "testCounter", value = 0))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }


    @Test
    fun testGetCounter() = testApplicationCustom {
        val client = createTestClient()

        client.post("/api/v1/counter") {
            contentType(ContentType.Application.Json)
            setBody(CounterDTO(name = "testCounter", value = 0))
        }

        val response = client.get("/api/v1/counter/testCounter")
        assertEquals(HttpStatusCode.OK, response.status)
        val counter = response.body<Int>()
        assertEquals(0, counter)
    }

    @Test
    fun testIncrementCounter() = testApplicationCustom {
        val client = createTestClient()

        client.post("/api/v1/counter") {
            contentType(ContentType.Application.Json)
            setBody(CounterDTO(name = "testCounter", value = 0))
        }

        val incrementResponse = client.post("/api/v1/counter/testCounter/increment")
        assertEquals(HttpStatusCode.OK, incrementResponse.status)

        val response = client.get("/api/v1/counter/testCounter")
        assertEquals(HttpStatusCode.OK, response.status)
        val counter = response.body<Int>()
        assertEquals(1, counter)
    }

    @Test
    fun testDeleteCounter() = testApplicationCustom {
        val client = createTestClient()

        client.post("/api/v1/counter") {
            contentType(ContentType.Application.Json)
            setBody(CounterDTO(name = "testCounter", value = 0))
        }

        val deleteResponse = client.delete("/api/v1/counter/testCounter")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)
        assertEquals("Counter 'testCounter' deleted.", deleteResponse.bodyAsText())

        val response = client.get("/api/v1/counter/testCounter")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("Counter 'testCounter' not found", response.bodyAsText())
    }

    @Test
    fun testGetAllCounters() = testApplicationCustom {
        val client = createTestClient()

        client.post("/api/v1/counter") {
            contentType(ContentType.Application.Json)
            setBody(CounterDTO(name = "counter1", value = 5))
        }

        client.post("/api/v1/counter") {
            contentType(ContentType.Application.Json)
            setBody(CounterDTO(name = "counter2", value = 10))
        }

        val response = client.get("/api/v1/counter")
        assertEquals(HttpStatusCode.OK, response.status)

        val counters = response.body<List<CounterDTO>>()
        val expectedCounters = listOf(
            CounterDTO(name = "counter1", value = 5),
            CounterDTO(name = "counter2", value = 10)
        )
        assertEquals(expectedCounters, counters)
    }
    @Test
    fun testAsyncInc() = testApplicationCustom {
        val client = createTestClient()
        val count = 10000
        client.post("/api/v1/counter") {
            contentType(ContentType.Application.Json)
            setBody(CounterDTO(name = "counter1", value = 0))
        }
        val jobs = (1..count).map {
            CoroutineScope(Dispatchers.IO).launch {
                val incrementResponse = client.post("/api/v1/counter/counter1/increment")
                assertEquals(HttpStatusCode.OK, incrementResponse.status)
            }
        }
        jobs.joinAll()

        val response = client.get("/api/v1/counter/counter1")
        assertEquals(HttpStatusCode.OK, response.status)
        val counter = response.body<Int>()
        assertEquals(count, counter)
    }
}