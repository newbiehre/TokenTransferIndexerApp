package com.example.demo.controllers

import com.example.demo.dtos.Erc721TokenDto
import com.example.demo.entities.Erc721TokenEntity
import com.example.demo.services.Erc721TokenIndexerService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for querying indexed tokens.
 *
 * @property erc721TokenIndexerService The service responsible for executing business logic related to ERC-721 Transfer Token events.
 */
@RestController
@RequestMapping("/api/tokens")
@Validated
class TokenIndexerController(
    private val erc721TokenIndexerService: Erc721TokenIndexerService,
) {

    /**
     * Endpoint for querying ERC-721 Transfer Token events based on filter criteria.
     *
     * This method accepts POST requests with a JSON body containing filter parameters for ERC721 tokens.
     * Returns HTTP status OK (200) along with the paginated list of tokens found or an empty list if there is no match.
     *
     * @param dto An ERC721TokenDto for filter criteria. Fields set to null are ignored.
     * @param pageable Pagination details with a default size set to 20 entries per page.
     * @return ResponseEntity containing a paginated list of ERC-721 Transfer Token events that match the given filters.
     */
    @PostMapping("/erc721")
    fun searchTokens(
        @Valid @RequestBody dto: Erc721TokenDto,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<Erc721TokenEntity>> { // todo
        val page = erc721TokenIndexerService.findAllErc721TokensByFilter(dto, pageable)
        return ResponseEntity.ok(page)
    }
}