package com.example.demo.services.subscription

import com.example.demo.services.factories.InfuraWebSocketServiceFactory
import com.example.demo.services.factories.Web3jFactory
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.web3j.protocol.Web3j

/**
 * Test class for ERC-721 Utils.
 *
 * Validates the behavior of Erc721Utils.checkIfErc721() method by making real calls
 * to the Ethereum network.
 */
@SpringBootTest
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class Erc721UtilsTest(
    @Autowired private val infuraWebSocketServiceFactory: InfuraWebSocketServiceFactory,
    @Autowired private val web3JFactory: Web3jFactory
) {
    private lateinit var web3j: Web3j

    @BeforeAll
    fun init() {
        val webSocketService = infuraWebSocketServiceFactory.create()
        try {
            webSocketService.connect()
            web3j = web3JFactory.build(webSocketService)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Valid contract address validation test for Erc721Utils.checkIfErc721().
     *
     * Utilizes real ERC-721 contract addresses to validate the functionality of
     * Erc721Utils.checkIfErc721() to verify that this method correctly identifies
     * contract addresses belonging to ERC-721 compliant tokens.
     */
    @ParameterizedTest
    @ValueSource(
        strings = [
            "0x60E4d786628Fea6478F785A6d7e704777c86a7c6", // MAYC
            "0xBd3531dA5CF5857e7CfAA92426877b022e612cf8", // PPG
            "0xED5AF388653567Af2F388E6224dC7C4b3241C544", // AZUKI
            "0xBC4CA0EdA7647A8aB7C2061c2E118A18a936f13D", // BAYC
        ]
    )
    fun `Should return true for valid ERC-721 contract addresses`(contractAddress: String) {
        val isValid = Erc721Utils.checkIfErc721(web3j, contractAddress)
        assertTrue(isValid, "Should return true for real valid ERC-721 contract addresses")
    }

    /**
     * Invalid contract address validation test for Erc721Utils.checkIfErc721().
     *
     * Utilizes incorrect or non-ERC-721 contract addresses to ensure that Erc721Utils.checkIfErc721()
     * correctly identifies them as invalid and does not return true for such addresses.
     */
    @ParameterizedTest
    @ValueSource(
        strings = [
            "0xb47e3cd837dDF8e4c57F05d70Ab865de6e193BBB", // CryptoPunks (doesn't implement ERC-721)
            "0xdac17f958d2ee523a2206206994597c13d831ec7", // ERC-20: Tether (USDT),
            "0x514910771af9ca656af840dff83e8264ecf986ca", // ERC-20: Chainlink (LINK)
            "0xf629cbd94d3791c9250152bd8dfbdf380e2a3b9c", // ERC-1155: Enjin Coin (ENK)
            "0xbbbbca6a901c926f240b89eacb641d8aec7aeafd", // ERC-777: Loopring (LRC)
            "wrong"
        ]
    )
    fun `Should return false for invalid or non-ERC-721 contract addresses`(contractAddress: String) {
        val isValid = Erc721Utils.checkIfErc721(web3j, contractAddress)
        assertFalse(isValid, "Should return false for wrong or non-ERC-721 contract addresses")
    }
}