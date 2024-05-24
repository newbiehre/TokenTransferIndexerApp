package com.example.demo.services.factories

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.web3j.protocol.websocket.WebSocketService

/**
 * Component class that creates a WebSocketService instance for connecting to the Infura WebSocket API.
 *
 * Infra is a popular choice that provides reliable WebSocket endpoints for EVM chain.
 *
 * @property infuraApiKey Api Key from a Infura account.
 */
@Component
class InfuraWebSocketServiceFactory(
    @Value("\${infura.apiKey}") private val infuraApiKey: String
) {

    /**
     * Creates and returns a WebSocketService instance for connecting to the Infura WebSocket API.
     *
     * @return WebSocketService instance configured with the Infura WebSocket URL and API key.
     */
    fun create(): WebSocketService {
        return WebSocketService("wss://mainnet.infura.io/ws/v3/$infuraApiKey", true)
    }
}