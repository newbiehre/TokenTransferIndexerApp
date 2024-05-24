package com.example.demo.controllers

import com.example.demo.services.subscription.Erc721SubscriptionService
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


/**
 * Unit test class for ERC-721 Subscription Controller.
 *
 * @WebMvcTest annotation is used to enable Spring MVC testing for the controller.
 */
@WebMvcTest(Erc721SubscriptionController::class)
@ExtendWith(MockitoExtension::class)
class Erc721SubscriptionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var erc721SubscriptionService: Erc721SubscriptionService

    private val url = "/api/subscription"

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Should get correct status and message after manual retry in establishing a subscription connection`(
        retryInitiated: Boolean
    ) {
        whenever(erc721SubscriptionService.manualRetrySubscriptionConnection()).thenReturn(retryInitiated)

        // Invoke
        val result = mockMvc.perform(post("$url/erc721/retry"))

        // Verify
        val status = if (retryInitiated) status().isAccepted else status().isOk
        val message = if (retryInitiated) "Retry successful." else "Connection already established."
        result
            .andExpect(status)
            .andExpect(MockMvcResultMatchers.content().string(message))
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Should get correct status and message when checking status of subscription connection`(isSuccessfullySubscribed: Boolean) {
        whenever(erc721SubscriptionService.getIsSuccessfullySubscribed()).thenReturn(isSuccessfullySubscribed)

        // Invoke
        val result = mockMvc.perform(get("$url/erc721/status"))

        // Verify
        val message = if (isSuccessfullySubscribed) "Subscription is active." else "Subscription is not active."
        result
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(message))
    }
}