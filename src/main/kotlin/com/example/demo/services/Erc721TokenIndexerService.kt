package com.example.demo.services

import com.example.demo.daos.Erc721TokenDao
import com.example.demo.dtos.Erc721TokenDto
import com.example.demo.entities.Erc721TokenEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Service responsible for handling operations related to ERC721 token data retrieval.
 *
 * This service acts as a mediator between the DAO layer and the controller, ensuring
 * that data fetched through complex queries is appropriately managed and transformed
 * before being passed to the client-facing part of the application. It utilizes the
 * ERC721TokenDao to interact directly with the database.
 *
 * @property erc721TokenDao The data access object (DAO) providing access to ERC721 token data.
 */
@Service
class Erc721TokenIndexerService(
    private val erc721TokenDao: Erc721TokenDao,
) {

    /**
     * Retrieves a page of ERC-721 Token Dto objects based on a specified filter and pagination settings.
     *
     * @param dto The ERC721TokenDto containing filter criteria such as block number, transaction hash, etc.
     * @param pageable A Pageable object specifying the pagination details like page number and size.
     * @return A Page containing ERC721TokenDto objects that match the filter criteria.
     */
    fun findAllErc721TokensByFilter(dto: Erc721TokenDto, pageable: Pageable): Page<Erc721TokenEntity> {
        return erc721TokenDao.findAllTokensByFilter(dto, pageable)
    }
}