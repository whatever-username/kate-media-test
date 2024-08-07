package com.whatever

import com.whatever.config.configureDatabaseAndGetDataSource
import com.whatever.repository.PostgresCounterRepository
import com.whatever.routing.counterRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    configureDatabaseAndGetDataSource()
    routing {
        swaggerUI(path = "swagger-ui", swaggerFile = "openapi/documentation.yaml")
        counterRoutes(PostgresCounterRepository())
    }
}
