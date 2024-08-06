package whatever.com

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import whatever.com.config.configureDatabaseAndGetDataSource
import whatever.com.repository.PostgresCounterRepository

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    val datasource = configureDatabaseAndGetDataSource()
    var counterRepository = PostgresCounterRepository()
}
