package com.whatever.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.log
import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

fun Application.configureDatabaseAndGetDataSource(): HikariDataSource {
    log.info("Initializing database")
    val dataSource = configureHikariDataSource()
    configureExposed(dataSource)
    runLiquibaseMigrations(dataSource)
    return dataSource
}


private fun Application.configureHikariDataSource() = HikariDataSource(HikariConfig().apply {
    driverClassName = environment.config.property("hikari.driverClassName").getString()
    jdbcUrl = environment.config.property("postgres.url").getString()
    username = environment.config.property("postgres.user").getString()
    password = environment.config.property("postgres.password").getString()
    maximumPoolSize = environment.config.property("hikari.maximumPoolSize").getString().toInt()
    isAutoCommit = false
    transactionIsolation = environment.config.property("hikari.transactionIsolation").getString()
    validate()
})

private fun Application.runLiquibaseMigrations(dataSource: DataSource) {
    log.info("Running Liquibase migrations")
    dataSource.connection.use { connection ->
        val databaseConnection = JdbcConnection(connection)
        val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection)
        val liquibase = Liquibase("db/changelog/master.xml", ClassLoaderResourceAccessor(), database)
        liquibase.update(Contexts())
    }
    log.info("Liquibase migrations have finished")
}

private fun Application.configureExposed(dataSource: DataSource) {
    Database.connect(dataSource)
}

