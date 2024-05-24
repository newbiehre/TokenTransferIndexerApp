package com.example.demo.services.subscription


import com.example.demo.common.TestFixtures
import com.example.demo.entities.Erc721TokenEntity
import com.example.demo.repositories.Erc721TokenRepository
import com.example.demo.services.factories.InfuraWebSocketServiceFactory
import com.example.demo.services.factories.Web3jFactory
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.websocket.WebSocketService
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture


/**
 * Unit test for ERC-721 Subscription Service.
 *
 * Verifies the behavior of the service related to establishing connections,
 * handling retries, initiating subscriptions and batch processing of events related to ERC-721 tokens.
 */
@ExtendWith(MockKExtension::class)
class Erc721SubscriptionServiceTest {

    @MockK
    private lateinit var repo: Erc721TokenRepository

    @MockK(relaxed = true)
    private lateinit var infuraWebSocketServiceFactory: InfuraWebSocketServiceFactory

    @MockK(relaxed = true)
    private lateinit var web3JFactory: Web3jFactory

    @MockK(relaxed = true)
    private lateinit var webSocketService: WebSocketService

    @MockK(relaxed = true)
    private lateinit var web3j: Web3j

    @MockK
    private lateinit var connectionExecutorService: ScheduledExecutorService

    @InjectMockKs
    private lateinit var service: Erc721SubscriptionService

    private lateinit var scheduledRetries: MutableList<ScheduledFuture<*>?>
    private lateinit var eventQueue: ConcurrentMap<String, MutableList<Erc721TokenEntity>>
    private val log = TestFixtures.transferEventLog

    @BeforeEach
    fun setup() {
        // Mock general expected behaviours
        every { infuraWebSocketServiceFactory.create() } returns webSocketService
        every { webSocketService.connect() } just Runs
        every { web3JFactory.build(any()) } returns web3j
        val runnableSlot = slot<Runnable>()
        every { connectionExecutorService.execute(capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
        }

        // Accessing private field using reflection
        val eventQueueField = Erc721SubscriptionService::class.java.getDeclaredField("eventQueue")
        eventQueueField.isAccessible = true
        eventQueue = eventQueueField.get(service) as ConcurrentMap<String, MutableList<Erc721TokenEntity>>

        val scheduledRetriesField = Erc721SubscriptionService::class.java.getDeclaredField("scheduledRetries")
        scheduledRetriesField.isAccessible = true
        scheduledRetries = scheduledRetriesField.get(service) as MutableList<ScheduledFuture<*>?>
    }

    @Nested
    inner class ConnectionAndRetries {

        @Test
        fun `Should successfully establish connection on post construct`() {
            mockSubscriptionEvent(log)

            // Invoke
            service.postConstruct()

            // Verify connection attempt was made
            verify(exactly = 1) {
                infuraWebSocketServiceFactory.create()
                webSocketService.connect()
                web3JFactory.build(webSocketService)
                scheduledRetries.clear()
            }
            verify(exactly = 0) {
                webSocketService.close()
                web3j.shutdown()
            }
            assertTrue(service.getIsSuccessfullySubscribed())
        }

        @Test
        fun `Should cancel scheduled retries if manualRetrySubscriptionConnection() is called`() {
            val initialRetries = setOf(
                getScheduledFutureMock(done = true, cancelled = false),
                getScheduledFutureMock(done = false, cancelled = true),
                getScheduledFutureMock(done = true, cancelled = true),
                null
            )
            scheduledRetries.addAll(initialRetries)

            // Invoke
            val result = service.manualRetrySubscriptionConnection()

            // Verify cancel() has been called for all scheduled retries
            scheduledRetries.forEach {
                verify { it?.cancel(any()) }
            }
            assertTrue(result)
            assertTrue(service.getIsSuccessfullySubscribed())
        }

        @Nested
        inner class ScheduleRetries {

            @BeforeEach
            fun setup() {
                every { webSocketService.connect() } throws RuntimeException("Connection failed")
            }

            @Test
            fun `Should clean up scheduled entries when schedule retry is triggered`() {
                // Initialize with existing scheduled retries
                val initialRetries = setOf(
                    getScheduledFutureMock(done = true, cancelled = false),
                    getScheduledFutureMock(done = false, cancelled = true),
                    null
                )
                scheduledRetries.addAll(initialRetries)
                assertTrue(scheduledRetries.containsAll(initialRetries), "Should contain initial retries")
                every { connectionExecutorService.schedule(any(), any(), any()) } returns mockk()

                // Invoke
                service.postConstruct()

                // Verify
                verify(exactly = 1) {
                    webSocketService.close()
                    web3j.shutdown()
                }
                assertEquals(
                    false, scheduledRetries.containsAll(initialRetries),
                    "Scheduled retries should not be retained if they are done, cancelled or null"
                )
            }

            @Test
            fun `Each scheduled retries added should have progressively bigger delays up to max number of attempts`() {
                val delaySlots = mutableListOf<CapturingSlot<Long>>()
                val mockFutures = mutableListOf<ScheduledFuture<*>>()
                repeat(5) {// Max attempt is 5
                    val delaySlot = slot<Long>()
                    delaySlots.add(delaySlot)
                    val mockFuture = getScheduledFutureMock(done = false, cancelled = false)
                    mockFutures.add(mockFuture)
                    every { connectionExecutorService.schedule(any(), capture(delaySlot), any()) } returns mockFuture

                    // Invoke
                    service.postConstruct()
                }

                // Verify that delay for each task gets progressively bigger up to max
                for (i in 0 until delaySlots.size - 1) {
                    val current = delaySlots[i].captured
                    val next = delaySlots[i + 1].captured
                    assertTrue(current <= next, "Expected $current to be less than or equal to $next")
                }
                assertTrue(
                    delaySlots.any { it.captured <= 16000L },
                    "Delay should not be bigger than max delay permitted"
                )
                assertTrue(scheduledRetries.containsAll(mockFutures), "Should contain all 5 futures")

                // Invoke beyond the max attempt permitted (6th time)
                service.postConstruct()

                // Verify this is nonetheless called only 5 times
                verify(exactly = 5) {
                    connectionExecutorService.schedule(any(), any(), any())
                }
            }
        }
    }

    @Nested
    inner class InitiateSubscription {

        @Test
        fun `Should add log to event queue when successfully initiated`() {
            mockSubscriptionEvent(log)

            // Invoke
            service.postConstruct()

            // Verify eventQueue stored the logs emitted by subscription
            verify(exactly = 1) { connectionExecutorService.execute(any()) }
            eventQueue[log.address] !![0].apply {
                assertNotNull(this)
                assertEquals(log.blockNumber, blockNumber)
                assertEquals(log.transactionHash, txHash)
                assertEquals(log.logIndex, logIndex)
                assertEquals(log.topics[0], senderAddress)
                assertEquals(log.topics[1], receiverAddress)
                assertEquals(log.topics[2], tokenId)
                assertEquals(log.address, contractAddress)
            }
        }

        @Test
        fun `Should trigger schedule retry when initiation not successful`() {
            mockSubscriptionError()
            val mockScheduledRetry = mockk<ScheduledFuture<*>>()
            every { connectionExecutorService.schedule(any(), any(), any()) } returns mockScheduledRetry

            // Invoke
            service.postConstruct()

            // Verify eventQueue stored the logs emitted by subscription
            assertTrue(eventQueue.isEmpty())
            verify(exactly = 1) {
                webSocketService.close()
                web3j.shutdown()
            }
            assertEquals(mockScheduledRetry, scheduledRetries.first())
            assertEquals(false, service.getIsSuccessfullySubscribed())
        }
    }

    @Nested
    inner class BatchProcessing {
        private val entity = TestFixtures.erc721TokenEntities[0]
        private lateinit var contractAddresses: Set<String>

        @BeforeEach
        fun setup() {
            mockSubscriptionEvent(log)
            mockkObject(Erc721Utils)

            // Invoke for set up
            service.postConstruct()

            eventQueue.clear() // Clear events saved from initiating service class
            contractAddresses = setOf(entity.contractAddress)
            eventQueue.getOrPut(entity.contractAddress) { mutableListOf() }.add(entity)
        }

        @Test
        fun `Should not need to verify contract address (call checkIfErc721())) if found in database`() {
            every { repo.findDistinctContractAddresses(contractAddresses) } returns contractAddresses

            // Invoke
            service.processTokenTransferBatch()

            // Verify event entity with valid contract address is saved to the database
            val savedEntitiesSlot = slot<Iterable<Erc721TokenEntity>>()
            verify(exactly = 1) { repo.saveAllAndFlush(capture(savedEntitiesSlot)) }
            assertEquals(entity, savedEntitiesSlot.captured.first())
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `Should verify contract address if not found & save valid event log to db`(isValidContractAddress: Boolean) {
            every { repo.findDistinctContractAddresses(contractAddresses) } returns emptySet()
            every { Erc721Utils.checkIfErc721(web3j, entity.contractAddress) } returns isValidContractAddress

            // Invoke
            service.processTokenTransferBatch()

            // Verify
            if (isValidContractAddress) {
                // Event entity with valid contract address is saved to the database
                val savedEntitiesSlot = slot<Iterable<Erc721TokenEntity>>()
                verify(exactly = 1) { repo.saveAllAndFlush(capture(savedEntitiesSlot)) }
                assertEquals(entity, savedEntitiesSlot.captured.first())
            } else {
                // Event entity with invalid contract address is not saved to the database
                verify(exactly = 0) { repo.saveAllAndFlush(any<List<Erc721TokenEntity>>()) }
            }
        }

        @Test
        fun `Should trigger initiateBatchProcessing() every 10 seconds`() {
            every { repo.findDistinctContractAddresses(contractAddresses) } returns contractAddresses

            // Invoke
            service.processTokenTransferBatch()

            // Verify event entity with valid contract address is saved to the database
            val savedEntitiesSlot = slot<Iterable<Erc721TokenEntity>>()
            verify(exactly = 1) { repo.saveAllAndFlush(capture(savedEntitiesSlot)) }
            assertEquals(entity, savedEntitiesSlot.captured.first())
        }
    }

    private fun getScheduledFutureMock(done: Boolean, cancelled: Boolean) =
        mockk<ScheduledFuture<*>> {
            every { isDone } returns done
            every { isCancelled } returns cancelled
            every { cancel(any()) } returns true
        }

    private fun mockSubscriptionEvent(log: Log): Log {
        val mockFlowable: Flowable<Log> = mockk()
        every { web3j.ethLogFlowable(Erc721Utils.TRANSFER_EVENT_TOPIC_ETH_FILTER) } returns mockFlowable

        val onNextSlot = slot<Consumer<Log>>()
        val onErrorSlot = slot<Consumer<Throwable>>()
        every { mockFlowable.subscribe(capture(onNextSlot), capture(onErrorSlot)) } answers {
            onNextSlot.captured.accept(log)
            mockk<Disposable>()
        }
        return log
    }

    private fun mockSubscriptionError() {
        val mockFlowable: Flowable<Log> = mockk()
        every { web3j.ethLogFlowable(Erc721Utils.TRANSFER_EVENT_TOPIC_ETH_FILTER) } returns mockFlowable

        val onNextSlot = slot<Consumer<Log>>()
        val onErrorSlot = slot<Consumer<Throwable>>()
        every { mockFlowable.subscribe(capture(onNextSlot), capture(onErrorSlot)) } answers {
            onErrorSlot.captured.accept(Exception("Some exception"))
            mockk<Disposable>()
        }
    }
}