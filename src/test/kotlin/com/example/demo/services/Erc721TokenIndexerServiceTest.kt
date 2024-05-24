package com.example.demo.services

import com.example.demo.common.TestFixtures
import com.example.demo.daos.Erc721TokenDao
import com.example.demo.dtos.Erc721TokenDto
import com.example.demo.entities.Erc721TokenEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

/**
 * Unit test class for ERC-721 Token Indexer Service.
 */
@ExtendWith(MockKExtension::class)
class Erc721TokenIndexerServiceTest {
    @MockK
    private lateinit var erc721TokenDao: Erc721TokenDao

    @InjectMockKs
    private lateinit var service: Erc721TokenIndexerService

    private val entities = TestFixtures.erc721TokenEntities

    @Test
    fun `findAllErc721TokensByFilter() should correctly return paginated entities`() {
        val mockDto: Erc721TokenDto = mockk()
        val mockPageable: Pageable = mockk()
        val mockPage: Page<Erc721TokenEntity> = PageImpl(entities)

        every { erc721TokenDao.findAllTokensByFilter(mockDto, mockPageable) } returns mockPage

        // Invoke
        val actualResult = service.findAllErc721TokensByFilter(mockDto, mockPageable).content

        // Verify
        assertEquals(entities, actualResult)
    }
}