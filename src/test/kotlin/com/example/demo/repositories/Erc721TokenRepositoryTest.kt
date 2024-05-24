package com.example.demo.repositories

import com.example.demo.common.TestDatabaseConfiguration
import com.example.demo.common.TestFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles


/**
 * Integration test for ERC-721 Repository.
 *
 * It loads the test-specific configuration defined in TestDatabaseConfiguration.
 * The application-test.properties file is used for configuring the test environment.
 *
 * Actual PostgreSQL instance required to ensure that SQL queries such as 'DISTINCT ON',
 * which is unique to PostgreSQL, behave as expected.
 */
@DataJpaTest
@Import(TestDatabaseConfiguration::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class Erc721TokenRepositoryTest(
    @Autowired private val repo: Erc721TokenRepository
) {
    private val erc721TokenEntities = TestFixtures.erc721TokenEntities

    @BeforeAll
    fun init() {
        repo.deleteAllInBatch()
        repo.saveAllAndFlush(erc721TokenEntities)
    }

    @Test
    fun `Should return all existing contract addresses found in database`() {
        val existingContractAddresses = repo.findAll().map { it.contractAddress }.toSet()
        val result = repo.findDistinctContractAddresses(existingContractAddresses).toSet()
        assertEquals(
            existingContractAddresses,
            result,
            "Should return the same result all existing contract addresses were used."
        )
    }

    @Test
    fun `Should return only existing contract addresses found in database`() {
        val existingContractAddress = repo.findAll().first().contractAddress
        val nonExistentContractAddress = "unknown_address"
        val result = repo.findDistinctContractAddresses(setOf(existingContractAddress, nonExistentContractAddress))
        assertEquals(setOf(existingContractAddress), result, "Should only return existing contract address.")
    }
}