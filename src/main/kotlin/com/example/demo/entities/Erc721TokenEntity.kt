package com.example.demo.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigInteger

/**
 * Entity representing an ERC-721 Transfer Token event.
 *
 * This entity stores information about ERC-721 Transfer Token event, which are captured
 * from the blockchain and persisted into the database for querying purposes.
 *
 * @property txHash Unique identifier for the transaction, generated based on the transaction details.
 * @property blockNumber The block number in which the transaction was included.
 * @property logIndex Index position of the log entry within the block, useful for identifying specific events.
 * @property senderAddress Ethereum address from which the token was sent.
 * @property receiverAddress Ethereum address to which the token was sent.
 * @property tokenId Unique identifier for the token within the ERC721 contract.
 * @property contractAddress Address of the ERC721 contract, unique for each deployed contract.
 */
@Entity
@Table(
    name = "erc721_tokens",
    indexes = [
        Index(name = "idx_contractAddress", columnList = "contractAddress")
    ]
)
data class Erc721TokenEntity(
    @Id
    val txHash: String,

    @Column(nullable = false)
    val blockNumber: BigInteger,

    @Column(nullable = false)
    val logIndex: BigInteger,

    @Column(nullable = false)
    val senderAddress: String,

    @Column(nullable = false)
    val receiverAddress: String,

    @Column(nullable = false)
    val tokenId: String,

    @Column(nullable = false)
    val contractAddress: String
)