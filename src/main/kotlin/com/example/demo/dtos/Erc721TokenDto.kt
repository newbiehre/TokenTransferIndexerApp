package com.example.demo.dtos

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigInteger

/**
 * Data Transfer Object (DTO) representing an ERC-721 Transfer Token event.
 *
 * Used to validate filter criteria from the controller class.
 *
 * @property txHash The transaction hash, a unique identifier for the transaction.
 * @property blockNumber The block number in which the transaction was recorded.
 * @property logIndex Index position of the log entry within the block, useful for identifying specific events.
 * @property senderAddress Ethereum address from which the token was sent.
 * @property receiverAddress Ethereum address to which the token was sent.
 * @property tokenId Unique identifier for the token within the ERC721 contract.
 * @property contractAddress Address of the ERC721 contract, unique for each deployed contract.
 */
data class Erc721TokenDto(
    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]{64}\$",
        message = "Tx Hash must be a valid 64-character hexadecimal string starting with '0x'."
    )
    val txHash: String? = null,

    @field:PositiveOrZero(
        message = "Block number must be a positive number or zero."
    )
    val blockNumber: BigInteger? = null,

    @field:PositiveOrZero(
        message = "Log Index must be a positive number or zero."
    )
    val logIndex: BigInteger? = null,

    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]{64}\$",
        message = "Sender address must be a valid 64-character hexadecimal string starting with '0x'."
    )
    val senderAddress: String? = null,

    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]{64}\$",
        message = "Receiver address must be a valid 64-character hexadecimal string starting with '0x'."
    )
    val receiverAddress: String? = null,

    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]{64}\$",
        message = "Token ID must be a valid 66-character hexadecimal string starting with '0x'."
    )
    val tokenId: String? = null,

    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]{40}\$",
        message = "Contract address must be a valid 42-character hexadecimal string starting with '0x'."
    )
    val contractAddress: String? = null,
)