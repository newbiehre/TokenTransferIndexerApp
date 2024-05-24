package com.example.demo.services.providers

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 * Provider for creating a single-threaded ScheduledExecutorService suitable for retries
 * of failed operations at increasing time intervals.
 */
@Component
class ScheduledExecutorServiceProvider {

    /**
     * Creates a new single-threaded ScheduledExecutorService.
     *
     * @return A new ScheduledExecutorService.
     */
    @Bean
    fun newScheduledThreadPool(): ScheduledExecutorService {
        return Executors.newSingleThreadScheduledExecutor()
    }
}