package com.example.demo.controllers

import com.example.demo.dtos.Erc721TokenDto
import com.example.demo.entities.Erc721TokenEntity
import com.example.demo.services.Erc721TokenIndexerService
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigInteger


/**
 * Unit test class for Token Indexer Controller.
 *
 * @WebMvcTest annotation is used to enable Spring MVC testing for the controller.
 */
@WebMvcTest(TokenIndexerController::class)
@ExtendWith(MockitoExtension::class)
class TokenIndexerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var erc721TokenIndexerService: Erc721TokenIndexerService

    private val url = "/api/tokens/erc721"
    private val pageMock = mock<PageImpl<Erc721TokenEntity>>()
    private val pageableMock = mock<PageRequest>()

    private fun invokeApiCall(dtoJson: String): ResultActions {
        return mockMvc.perform(
            post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dtoJson)
                .accept(MediaType.APPLICATION_JSON)
        )
    }

    @Test
    fun `Should get ok status and result if dto field values are all null`() {
        val validDto = Erc721TokenDto()
        val dtoJson = objectMapper.writeValueAsString(validDto)
        whenever(erc721TokenIndexerService.findAllErc721TokensByFilter(validDto, pageableMock)).thenReturn(pageMock)

        // Invoke
        val result = invokeApiCall(dtoJson)

        // Verify
        result.andExpect(status().isOk)
    }

    @Test
    fun `Should get ok status and result if dto field values are valid for every field`() {
        val validDto = Erc721TokenDto(
            blockNumber = BigInteger.valueOf(19840126),
            txHash = "0x86401dbb005e3f1534feeb770e9ddbb9519fe2ea5153d89b3fc92a890a2eca0c",
            logIndex = BigInteger.valueOf(3),
            senderAddress = "0x000000000000000000000000662e8882b956899fd29b7890441b7889d1b124ff",
            receiverAddress = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
            tokenId = "0x000000000000000000000000ca74f404e0c7bfa35b13b511097df966d5a65597",
            contractAddress = "0xdbcd57cc74b180f928258f7b1a32f6f7e64bf12e"
        )
        val dtoJson = objectMapper.writeValueAsString(validDto)
        whenever(erc721TokenIndexerService.findAllErc721TokensByFilter(validDto, pageableMock)).thenReturn(pageMock)

        // Invoke
        val result = invokeApiCall(dtoJson)

        // Verify
        result.andExpect(status().isOk)
    }

    @Test
    fun `Should get bad request status and matching validation errors for the invalid values used in dto`() {
        val invalidDto = Erc721TokenDto(
            blockNumber = BigInteger.valueOf(- 19840126),
            txHash = "wrong"
        )
        val dtoJson = objectMapper.writeValueAsString(invalidDto)
        whenever(erc721TokenIndexerService.findAllErc721TokensByFilter(invalidDto, pageableMock)).thenReturn(pageMock)

        // Invoke
        val result = invokeApiCall(dtoJson)

        // Verify
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(
                jsonPath(
                    "$.message", containsInAnyOrder(
                        "Block number must be a positive number or zero.",
                        "Tx Hash must be a valid 64-character hexadecimal string starting with '0x'."
                    )
                )
            )
            .andExpect(jsonPath("$.path").value("/api/tokens/erc721"))
    }

    @Test
    fun `Should get bad request status and all validation errors when using invalid values for dto`() {
        val invalidAddress = "012"
        val invalidBigInt = BigInteger.valueOf(- 1)
        val invalidDto = Erc721TokenDto(
            blockNumber = invalidBigInt,
            txHash = invalidAddress,
            logIndex = invalidBigInt,
            senderAddress = invalidAddress,
            receiverAddress = invalidAddress,
            tokenId = invalidAddress,
            contractAddress = invalidAddress
        )
        val dtoJson = objectMapper.writeValueAsString(invalidDto)
        whenever(erc721TokenIndexerService.findAllErc721TokensByFilter(invalidDto, pageableMock)).thenReturn(pageMock)

        // Invoke
        val result = invokeApiCall(dtoJson)

        // Verify
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(
                jsonPath(
                    "$.message", containsInAnyOrder(
                        "Contract address must be a valid 42-character hexadecimal string starting with '0x'.",
                        "Receiver address must be a valid 64-character hexadecimal string starting with '0x'.",
                        "Block number must be a positive number or zero.",
                        "Sender address must be a valid 64-character hexadecimal string starting with '0x'.",
                        "Log Index must be a positive number or zero.",
                        "Token ID must be a valid 66-character hexadecimal string starting with '0x'.",
                        "Tx Hash must be a valid 64-character hexadecimal string starting with '0x'."
                    )
                )
            )
            .andExpect(jsonPath("$.path").value("/api/tokens/erc721"))
    }
}