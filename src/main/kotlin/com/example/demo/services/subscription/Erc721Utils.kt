package com.example.demo.services.subscription

import org.slf4j.LoggerFactory
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Bytes4
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.utils.Numeric

/**
 * Utility object for working with ERC-721 contracts using Web3j.
 *
 * Reference (https://docs.web3j.io/4.11.0/transactions/transactions_and_smart_contracts/)
 */
object Erc721Utils {
    private const val ERC721_INTERFACE_ID = "0x80ac58cd"
    val TRANSFER_EVENT_TOPIC_ETH_FILTER = createTransferEventTopicEthFilter()
    private val supportInterfaceCheck = erc721SupportInterfaceFunction()

    private var logger = LoggerFactory.getLogger(Erc721Utils::class.java)

    /**
     * Checks if a given contract address supports the ERC721 interface.
     *
     * @param web3j Web3j instance
     * @param contractAddress Contract address to check
     * @return True if the contract supports the ERC721 interface, false otherwise
     */
    fun checkIfErc721(web3j: Web3j, contractAddress: String): Boolean {
        val (encodedFunction, outputParameters) = supportInterfaceCheck
        return try {
            val response = web3j.ethCall(
                Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).sendAsync().get()

            val output = FunctionReturnDecoder.decode(response.value, outputParameters)
            if (output.isEmpty()) {
                logger.debug("Contract address [$contractAddress]: output is empty")
                false
            } else {
                val result = (output[0] as Bool).value
                logger.debug("Contract address [$contractAddress]: interface is supported [$result]")
                result
            }
        } catch (e: Exception) {
            logger.error("Error: ${e.message} for contract: $contractAddress")
            false
        }
    }

    /**
     * Encodes the 'supportsInterface' function of ERC721.
     *
     * @return A pair containing the encoded function and the list of output parameter types.
     */
    private fun erc721SupportInterfaceFunction(): Pair<String, MutableList<TypeReference<Type<Any>>>> {
        val bytes4Array = Bytes4(Numeric.hexStringToByteArray(ERC721_INTERFACE_ID))
        val function = Function(
            "supportsInterface",
            listOf(bytes4Array),
            listOf<TypeReference<*>>(TypeReference.create(Bool::class.java))
        )
        return FunctionEncoder.encode(function) to function.outputParameters
    }

    /**
     * Creates an Ethereum filter for ERC-721 Transfer Events.
     *
     * @return The Ethereum filter for ERC-721 Transfer Events.
     */
    private fun createTransferEventTopicEthFilter(): EthFilter {
        val transferEvent = Event(
            "Transfer",
            listOf(
                TypeReference.create(Address::class.java, true),
                TypeReference.create(Address::class.java, true),
                TypeReference.create(Uint256::class.java, true)
            )
        )
        val eventSignature = EventEncoder.encode(transferEvent)
        return EthFilter(
            DefaultBlockParameterName.LATEST,
            DefaultBlockParameterName.LATEST,
            listOf() // contract addresses
        ).addSingleTopic(eventSignature)
    }
}