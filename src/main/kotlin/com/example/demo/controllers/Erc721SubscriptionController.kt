package com.example.demo.controllers

import com.example.demo.services.subscription.Erc721SubscriptionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing ERC-721 Transfer Token event subscription.
 *
 * Provides endpoints to retry establishing a subscription connection,
 * and retrieve the current connection status.
 *
 * @property erc721SubscriptionService The service responsible for handling subscription-related operations for ERC-721 tokens.
 */
@RestController
@RequestMapping("/api/subscription/erc721")
class Erc721SubscriptionController(
    private val erc721SubscriptionService: Erc721SubscriptionService
) {

    /**
     * Endpoint to trigger manual retry of the subscription connection.
     *
     * Returns 202 ACCEPTED if retry started (i.e. accepted for processing but likely not completed),
     * 200 OK if already connected.
     *
     * @return Response entity with status message
     */
    @PostMapping("/retry")
    fun retrySubscription(): ResponseEntity<String> {
        val success = erc721SubscriptionService.manualRetrySubscriptionConnection()
        return if (success) ResponseEntity.accepted()
            .body("Retry successful.") // Accepted meaning accepted for processing but likely not completed
        else ResponseEntity.ok("Connection already established.")
    }

    /**
     * Endpoint to get the current status of the subscription connection.
     *
     * Returns 200 OK with status message indicating active or inactive.
     *
     * @return Response entity with status message
     */
    @GetMapping("/status")
    fun subscriptionConnectionStatus(): ResponseEntity<String> {
        val isSubscribed = erc721SubscriptionService.getIsSuccessfullySubscribed()
        val message = if (isSubscribed) "Subscription is active." else "Subscription is not active."
        return ResponseEntity.ok(message)
    }
}