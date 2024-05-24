package com.example.demo.services.factories

import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j
import org.web3j.protocol.websocket.WebSocketService

/**
 * Component class that provides instances of Web3j.
 */
@Component
class Web3jFactory {

    /**
     * Builds and returns a Web3j instance using the provided WebSocketService.
     *
     * @param webSocketService WebSocketService implementation to be used for the Web3j instance.
     * @return Web3j instance built using the specified WebSocketService.
     */
    fun build(webSocketService: WebSocketService): Web3j {
        return Web3j.build(webSocketService)
    }
}