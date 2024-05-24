package com.example.demo.daos

import com.example.demo.common.TestDatabaseConfiguration
import com.example.demo.common.TestFixtures
import com.example.demo.dtos.Erc721TokenDto
import com.example.demo.entities.Erc721TokenEntity
import com.example.demo.repositories.Erc721TokenRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles


/**
 * Integration test class for ERC-721 Token Dao.
 *
 * It loads the test-specific configuration defined in TestDatabaseConfiguration.
 * The application-test.properties file is used for configuring the test environment.
 *
 * Actual PostgreSQL instance required to how entity manager is used in ERC-721 Token Dao service.
 */
@DataJpaTest
@Import(TestDatabaseConfiguration::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class Erc721TokenDaoTest(
    @Autowired private val entityManager: EntityManager,
    @Autowired private val repo: Erc721TokenRepository
) {
    private lateinit var dao: Erc721TokenDao

    private val pageable = PageRequest.of(0, 10)
    private val entities = TestFixtures.erc721TokenEntities

    @BeforeAll
    fun init() {
        dao = Erc721TokenDao(entityManager)
        repo.deleteAllInBatch()
        repo.saveAllAndFlush(entities)
    }

    @Test
    fun `Should find one token with exact entity match`() {
        val filterDto = convertEntityToDto(entities[0])

        val actualResult = dao.findAllTokensByFilter(filterDto, pageable).content
        assertEquals(entities[0], actualResult.first())
    }

    @Test
    fun `Should find two tokens with the same blockNumber`() {
        val blockNumber = entities[0].blockNumber
        assertEquals(blockNumber, entities[1].blockNumber, "Both entity's blockNumber should be the same for this test")
        val filterDto = Erc721TokenDto(blockNumber = blockNumber)

        val expectedResult = listOf(entities[0], entities[1])
        val actualResult = dao.findAllTokensByFilter(filterDto, pageable).content
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `Should return empty list if no match is found`() {
        val filterDto = Erc721TokenDto(contractAddress = "non-existent-contract-address")

        val actualResult = dao.findAllTokensByFilter(filterDto, pageable).content
        assertEquals(emptyList<Erc721TokenEntity>(), actualResult)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 10])
    fun `Should return everything when no filter are specified with correct pagination`(pageSize: Int) {
        val filterDto = Erc721TokenDto()
        val customPageable = PageRequest.of(0, pageSize)

        val actualResult = dao.findAllTokensByFilter(filterDto, customPageable)
        when (pageSize) {
            1 -> assertEquals(PageImpl(listOf(entities[0]), customPageable, 1), actualResult)
            2 -> assertEquals(PageImpl(entities.subList(0, 2), customPageable, 2), actualResult)
            10 -> assertEquals(PageImpl(entities, customPageable, 4), actualResult)
        }
    }

    private fun convertEntityToDto(entity: Erc721TokenEntity): Erc721TokenDto {
        return Erc721TokenDto(
            blockNumber = entity.blockNumber,
            txHash = entity.txHash,
            logIndex = entity.logIndex,
            senderAddress = entity.senderAddress,
            receiverAddress = entity.receiverAddress,
            tokenId = entity.tokenId,
            contractAddress = entity.contractAddress
        )
    }
}