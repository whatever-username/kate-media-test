package com.whatever.routing

import com.whatever.model.CounterDTO
import com.whatever.repository.PostgresCounterRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route


fun Routing.counterRoutes(counterRepository: PostgresCounterRepository) {
    route("/api/v1/counter") {

        // CREATE
        post {
            val request = call.receive<CounterDTO>()
            counterRepository.createCounter(request.name, request.value)
            call.respond(HttpStatusCode.Created)
        }

        // READ
        get("{name}") {
            val name = call.parameters["name"]
            if (name != null) {
                val counter = counterRepository.getCounter(name)
                if (counter != null) {
                    call.respond(counter)
                } else {
                    call.respondText("Counter '$name' not found", status = HttpStatusCode.NotFound)
                }
            } else {
                call.respondText("Missing counter name", status = HttpStatusCode.BadRequest)
            }
        }

        // INCREMENT
        post("{name}/increment") {
            val name = call.parameters["name"]
            if (name != null) {
                counterRepository.incrementCounter(name)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respondText("Missing counter name", status = HttpStatusCode.BadRequest)
            }
        }

        // DELETE
        delete("{name}") {
            val name = call.parameters["name"]
            if (name != null) {
                val deleted = counterRepository.deleteCounter(name)
                if (deleted) {
                    call.respondText("Counter '$name' deleted.")
                } else {
                    call.respondText("Counter '$name' not found", status = HttpStatusCode.NotFound)
                }
            } else {
                call.respondText("Missing counter name", status = HttpStatusCode.BadRequest)
            }
        }

        // GET ALL
        get {
            call.respond(counterRepository.getAllCounters())
        }
    }
}
