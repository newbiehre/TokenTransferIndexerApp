package com.example.demo.common

import com.example.demo.entities.Erc721TokenEntity
import java.math.BigInteger
import org.web3j.protocol.core.methods.response.Log

/**
 * Utility object for creating test fixtures.
 *
 * This includes generating predefined blockchain transfer event logs and
 * ERC-721 token entities to be used across multiple tests
 * to ensure consistency and reduce redundancy in test setups.
 */
object TestFixtures {

    /**
     * Public property that initializes a list of ERC-721 token entities for testing.
     */
    val erc721TokenEntities = createErc721TokenEntities()

    /**
     * Public property that initializes a blockchain log entry for testing.
     */
    val transferEventLog = createTransferEventLog()

    /**
     * Creates a sample blockchain log entry representing a token transfer event.
     * This log is structured based on the Ethereum blockchain's event logging system.
     *
     * @return Log object configured with predefined values mimicking a blockchain transaction.
     */
    private fun createTransferEventLog(): Log {
        return Log(
            false,
            "100",
            "1",
            "0x1d2a2ca5e38889a57e523fabef2ec4db16f256945b5709c583352c2fce670dbb",
            "0xabcdef",
            "11887220",
            "0x332261f9fc8da46c4a22e31b45c4de60623848bf",
            "0xdata",
            "type",
            listOf(
                "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                "0x0000000000000000000000001de05431103e2f00f1a315313c9acb479b5f5b3a",
                "0x00000000000000000000000078f8ebe30bc7306c1b023ad51476dbfd1c87b230"
            )
        )
    }

    /**
     * Creates a list of sample ERC-721 Token Entities.
     *
     * @return A list of ERC-721 Token Entity configured with random transaction details.
     */
    private fun createErc721TokenEntities(): List<Erc721TokenEntity> {
        val sharedContractAddress = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"

        return listOf(
            Erc721TokenEntity(
                blockNumber = BigInteger("19840126"),
                txHash = "0x86401dbb005e3f1534feeb770e9ddbb9519fe2ea5153d89b3fc92a890a2eca0c",
                logIndex = BigInteger("3"),
                senderAddress = "0x000000000000000000000000662e8882b956899fd29b7890441b7889d1b124ff",
                receiverAddress = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                tokenId = "0x000000000000000000000000ca74f404e0c7bfa35b13b511097df966d5a65597",
                contractAddress = "0x44f5909e97e1cbf5fbbdf0fc92fd83cde5d5c58a"
            ),
            Erc721TokenEntity(
                blockNumber = BigInteger("19840126"),
                txHash = "0xb617bfc1ceaa627d7dc00efe4da5c7d38fe8e5fef70880602245b038cea6c2e3",
                logIndex = BigInteger("7"),
                senderAddress = "0x000000000000000000000000662e8882b956899fd29b7890441b7889d1b124ff",
                receiverAddress = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                tokenId = "0x000000000000000000000000946fee835b6d520c0107e12fbdb04c7ef7b07f76",
                contractAddress = "0xc9eb61ffb66d5815d643bbb8195e17c49687ae1e"
            ),
            Erc721TokenEntity(
                blockNumber = BigInteger("19840127"),
                txHash = "0xb617bfc1ceaa627d7dc00efe4da5c7d38fe8e5fef70880602245b038cea6c2e1",
                logIndex = BigInteger("31"),
                senderAddress = "0x00000000000000000000000080c1969588bd9a017190ff4ed669e4e4b70e7768",
                receiverAddress = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                tokenId = "0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d",
                contractAddress = sharedContractAddress
            ),
            Erc721TokenEntity(
                blockNumber = BigInteger("19840128"),
                txHash = "0xb617bfc1ceaa627d7dc00efe4da5c7d38fe8e5fef70880602245b038cea6c2e1=2",
                logIndex = BigInteger("344"),
                senderAddress = "0x0000000000000000000000006b75d8af000000e20b7a7ddf000ba900b4009a80",
                receiverAddress = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                tokenId = "0x0000000000000000000000006b75d8af000000e20b7a7ddf000ba900b4009a80",
                contractAddress = sharedContractAddress
            )
        )
    }
}