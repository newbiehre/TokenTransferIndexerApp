package com.example.demo.common

import com.github.dockerjava.api.exception.NotFoundException
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.containers.PostgreSQLContainer

/**
 * Test configuration class for setting up a PostgreSQL database container.
 *
 * This is used to define beans for integration tests, ensuring that each test
 * operates against a clean and isolated instance of the database.
 *
 * This class uses Testcontainers to start a PostgreSQL container. It automatically configures
 * a data source bean that points to this containerized database.
 *
 * @throws NotFoundException when unable to start PostgreSQL container, often due to Docker not running.
 */
@TestConfiguration
class TestDatabaseConfiguration {

    @Value("\${spring.datasource.username}")
    private lateinit var username: String

    @Value("\${spring.datasource.password}")
    private lateinit var password: String

    private val dbName = "indexer_test"

    /**
     * Configures and starts a PostgreSQL container using the Testcontainers library.
     * The container uses the latest PostgreSQL image.
     *
     * @return A running PostgreSQLContainer configured with specified username, password, and database name.
     */
    @Bean
    fun postgresContainer(): PostgreSQLContainer<*> {
        try {
            val postgresContainer = PostgreSQLContainer("postgres:latest")
                .withDatabaseName(dbName)
                .withUsername(username)
                .withPassword(password)
            postgresContainer.start()
            return postgresContainer
        } catch (e: Exception) {
            throw NotFoundException("Check to ensure the local docker is up and running ($e)")
        }
    }

    /**
     * Creates a DataSource bean that is configured to connect to the PostgreSQL container.
     * This DataSource is marked as @Primary to ensure that it is used preferentially over any other DataSource beans
     * in the Spring context during tests.
     *
     * @param postgresContainer The PostgreSQL container instance from which JDBC connection parameters are extracted.
     * @return DataSource configured to connect to the initialized PostgreSQL container.
     */
    @Bean
    @Primary
    fun dataSource(postgresContainer: PostgreSQLContainer<*>): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName("org.postgresql.Driver")
        dataSource.url = postgresContainer.jdbcUrl
        dataSource.username = postgresContainer.username
        dataSource.password = postgresContainer.password
        return dataSource
    }
}