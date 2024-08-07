package com.whatever

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class Database {

    val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:15.2"))
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .also { it.start() }


}