package com.example.demo.services.subscription

import com.example.demo.entities.Erc721TokenEntity
import com.example.demo.repositories.Erc721TokenRepository
import com.example.demo.services.factories.InfuraWebSocketServiceFactory
import com.example.demo.services.factories.Web3jFactory
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.transaction.Transactional
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.web3j.protocol.Web3j
import org.web3j.protocol.websocket.WebSocketService

/**
 * Service class for ERC-721 Subscription Service.
 *
 * Provides the functionality for establishing Web3j, websocket and subscription connections, retrying connection attempts,
 * manual retry initiation, batch processing of transfer events and graceful shutdown of resources.
 *
 * @property repo Erc721TokenRepository to store data into database.
 * @property infuraWebSocketServiceFactory The factory that creates and returns a WebSocketService instance for connecting to the Infura WebSocket API.
 * @property web3JFactory The factory that builds and returns a Web3j instance.
 * @property connectionExecutorService The provider of a single thread scheduled executor. See ScheduledExecutorServiceProvider.
 */
@Service
class Erc721SubscriptionService(
    private val repo: Erc721TokenRepository,
    private val infuraWebSocketServiceFactory: InfuraWebSocketServiceFactory,
    private val web3jFactory: Web3jFactory,
    private val connectionExecutorService: ScheduledExecutorService
) {
    private var logger = LoggerFactory.getLogger(Erc721SubscriptionService::class.java)

    private lateinit var webSocketService: WebSocketService // WebSocket connection used to stream for real-time notifications from the blockchain
    private lateinit var web3j: Web3j // Library supported for Kotlin

    private val eventQueue: ConcurrentMap<String, MutableList<Erc721TokenEntity>> =
        ConcurrentHashMap() // Thread-safe operation

    // Dynamic thread creation as needed or reuse if available for short-lived asynchronous tasks,
    // removing idle (60 seconds of inactivity, plenty of time for processing)
    private val batchProcessingExecutorService: ExecutorService = Executors.newCachedThreadPool()

    private var attempt = 0
    private val maxAttempts = 5
    private val initialRetryDelay = 5000L // 5 seconds
    private val maxDelay = 16000L // 16 seconds

    private val scheduledRetries = Collections.synchronizedList(mutableListOf<ScheduledFuture<*>?>())

    @Volatile // Ensure visibility across threads
    private var isSuccessfullySubscribed = false

    /**
     * Initializes service by attempting to establish a connection.
     */
    @PostConstruct
    fun postConstruct() {
        tryEstablishingConnections()
    }

    /**
     * Tries to establish connection subscription to the blockchain network.
     *
     * If connection is already established, no further action is taken.
     * Handles connection failures by scheduling retries.
     */
    private fun tryEstablishingConnections() {
        if (isSuccessfullySubscribed) {
            logger.warn("Connection already established. No need to retry...")
            return
        }

        // Immediate execution or placed in queue as soon as the thread becomes available.
        // If the thread is currently idle, it will execute immediately. If the thread is busy, it will wait until the current task is complete.
        connectionExecutorService.execute { // Immediate asynchronous execution
            try {
                initiateWebSocketConnection()
                initiateSubscription()
                isSuccessfullySubscribed = true
                attempt = 0 // Reset count when successful
                scheduledRetries.clear() // Clear any scheduled retries lined up
            } catch (e: Exception) {
                logger.error("Failed to connect. Retrying...")
                isSuccessfullySubscribed = false
                webSocketService.close()
                web3j.shutdown()
                scheduleRetry()
            }
        }
    }

    /**
     * Schedules a retry to establish connections again.
     *
     * Cleans up completed or canceled scheduled retries first before ensuring that
     * connection has not been established yet and maximum number of retry has not been reached before
     * scheduling a new retry task. Uses an exponential backoff strategy for delay calculation to mitigate
     * high frequency retry impact.
     */
    private fun scheduleRetry() {
        // Clean up scheduled retries
        scheduledRetries.removeIf { it == null || it.isDone || it.isCancelled }

        if (! isSuccessfullySubscribed && attempt < maxAttempts) {
            val delay = calculateExponentialBackoff()
            // Delayed Execution with specified delay in queue
            scheduledRetries.add(
                connectionExecutorService.schedule(
                    this::tryEstablishingConnections,
                    delay,
                    TimeUnit.MILLISECONDS
                )
            )
            logger.info("Scheduled retry at [$attempt] attempt (delay: $delay ms).")
            attempt ++
        } else {
            logger.error("Max retry attempts reached. Unable to connect to blockchain.")
        }
    }

    /**
     * Retrieves the status of subscription connection.
     *
     * @return Boolean indicating the subscription connection is active (i.e. true) or not (i.e. false).
     */
    fun getIsSuccessfullySubscribed(): Boolean {
        return isSuccessfullySubscribed
    }

    /**
     * Manual retry for establishing a failed subscription connection.
     *
     * If not already connected, this method resets attempt to 0, cancels any scheduled retries and cleans up the queue
     * before trying to establish the connection again. Immediately returns whether retry has been initiated or not, not
     * the result of the reconnection attempt.
     *
     * @return Boolean indicating if the retry was triggered (i.e. true) or not (i.e. false).
     */
    fun manualRetrySubscriptionConnection(): Boolean {
        if (! isSuccessfullySubscribed) {
            attempt = 0

            // Cancel any existing scheduled retries
            logger.info("Manual retry initiated by user. Cancelled ${scheduledRetries.size} scheduled retries.")
            scheduledRetries.forEach { it?.cancel(false) }
            // Clean up on scheduledRetries
            scheduledRetries.removeIf { it == null || it.isDone || it.isCancelled }

            tryEstablishingConnections()
            return true
        }
        return false
    }

    /**
     * Calculates the delay for the next retry attempt using an exponential backoff formula.
     *
     * Exponential backoff helps to reduce the load on the network and increase the
     * chances of successful reconnection by spacing out retries.
     *
     * @return the calculated delay in milliseconds, ensuring it does not exceed the predefined maximum delay.
     */
    private fun calculateExponentialBackoff(): Long {
        val exponentialBackoff = initialRetryDelay * Math.pow(2.0, (attempt - 1).toDouble())
        return exponentialBackoff.coerceAtMost(maxDelay.toDouble()).toLong()
    }

    /**
     * Initiates websocket connection with Infura and to the ethereum network.
     */
    private fun initiateWebSocketConnection() {
        try {
            webSocketService = infuraWebSocketServiceFactory.create()
            webSocketService.connect()
            web3j = web3jFactory.build(webSocketService)
            logger.info("Websocket connection with Web3j established successfully!")
        } catch (e: Exception) {
            logger.error("Failed to establish websocket connection with Web3j connection: ${e.message}.")
            throw e
        }
    }

    /**
     * Initiates subscription to ERC-721 Token Transfer events.
     *
     * This method sets up subscription handling of Transfer Token event on the ethereum
     * blockchain and maps the log events emitted before storing it into an event queue
     * for asynchronous processing that is handled separately.
     */
    private fun initiateSubscription() {
        logger.info("Initiating web3j subscription connection...")
        web3j.ethLogFlowable(Erc721Utils.TRANSFER_EVENT_TOPIC_ETH_FILTER).subscribe(
            { log ->
                val entity = Erc721TokenEntity(
                    blockNumber = log.blockNumber,
                    txHash = log.transactionHash,
                    logIndex = log.logIndex,
                    senderAddress = log.topics[0],
                    receiverAddress = log.topics[1],
                    tokenId = log.topics[2],
                    contractAddress = log.address
                )
                logger.debug("Entity (${entity.txHash}) retrieved from subscription.")

                synchronized(eventQueue) {
                    eventQueue.getOrPut(entity.contractAddress) { mutableListOf() }.add(entity)
                }
            },
            { error ->
                logger.error("Error in subscription. Attempting to reconnect...", error)
                throw error
            }
        )
    }

    /**
     * Processes  batch processing of Token Transfer events stored in event queue.
     *
     * This method is scheduled to run every 1 second, handled by Spring context.
     *
     * If event queue is not empty, this method submits an asynchronous task to
     * store current events from event queue separately, then empty the event queue, and validate the current events
     * using handleCallToBlockchain() function. Once the results of validated ERC-721 Token Transfer
     * entities are returned, they are saved and flushed into the database.
     */
    @Transactional
    @Scheduled(fixedRate = 1000) // Scheduled to run every 1 second
    fun processTokenTransferBatch() {
        if (eventQueue.isEmpty()) return

        // Allows tasks to be executed asynchronously on diff threads. Once completed, thread can be reused for future tasks.
        batchProcessingExecutorService.submit {
            val eventsToCheck = mutableMapOf<String, MutableList<Erc721TokenEntity>>()

            synchronized(eventQueue) {
                logger.info("Batch processing [${eventQueue.values.flatten().size}] transfer event logs ([${eventQueue.size}] contract addresses to process)...")
                eventsToCheck.putAll(eventQueue)
                eventQueue.clear()
            }

            handleCallToBlockchain(eventsToCheck).apply {
                if (isNotEmpty()) {
                    repo.saveAllAndFlush(this)
                    logger.info("[${size}] ERC-721 transfer event logs saved to database")
                }
            }
        }
    }

    /**
     * Handles call to the blockchain to validate provided contract addresses.
     *
     * This method checks if contract addresses already exists in the database. If so,
     * these are already verified. Only contract addresses not found in the database
     * will be validated using Erc721Utils.checkIfErc721().
     *
     * Events with valid contract addresses will then be returned.
     *
     * @param transferEventLogs A mutable map containing the contract address and a list of ERC-721 Token Entities with that contract address.
     * @return A list of ERC-721 Token Entities with valid contract addresses.
     */
    private fun handleCallToBlockchain(transferEventLogs: MutableMap<String, MutableList<Erc721TokenEntity>>): List<Erc721TokenEntity> {
        val validatedContractAddresses = repo.findDistinctContractAddresses(transferEventLogs.keys)
        logger.info("Found [${validatedContractAddresses.size}] matching contract addresses in the database")

        val newValidContractAddresses = transferEventLogs
            .filterNot { validatedContractAddresses.contains(it.key) }.keys
            .let { listToValidate ->
                logger.info("Validating [${listToValidate.size}] new contract addresses on the blockchain...")
                if (listToValidate.isNotEmpty()) {
                    listToValidate.mapNotNull { contractAddress ->
                        val isERC721 = Erc721Utils.checkIfErc721(web3j, contractAddress)
                        if (isERC721) contractAddress else null
                    }.toSet()
                } else {
                    emptySet()
                }
            }.also { logger.info("Validated [${it.size}]") }

        transferEventLogs.keys.retainAll(validatedContractAddresses + newValidContractAddresses)
        logger.info("[${transferEventLogs.keys.size}] validated contract address retained, total [${transferEventLogs.values.flatten().size}] events to save to database")
        return transferEventLogs.values.flatten()
    }


    /**
     * Gracefully shuts down the resources used by the ERC-721 subscription service.
     *
     * This method closes the WebSocket connection, shuts down the Web3j instance,
     * cancels all scheduled retries, disallow new tasks, cancel currently executing tasks and wait to ensure
     * it is terminated.
     */
    @PreDestroy
    fun preDestroy() {
        try {
            logger.info("Shutting down ERC721 subscription service...")
            webSocketService.close()
            web3j.shutdown()
            scheduledRetries.forEach { it?.cancel(false) } // Cancel any scheduled retries
            // Initiate an orderly shutdown by disallowing new tasks
            connectionExecutorService.shutdown()
            batchProcessingExecutorService.shutdown()

            // Wait a while for existing tasks to terminate
            if (! connectionExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                connectionExecutorService.shutdownNow()
            }
            if (! batchProcessingExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                batchProcessingExecutorService.shutdownNow()
            }

            // Wait again after calling shutdownNow to ensure tasks are terminated
            if (! connectionExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Connection Executor Service did not terminate")
            }
            if (! batchProcessingExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Batch Processing Executor Service did not terminate")
            }
        } catch (ie: InterruptedException) {
            // (Re-)Cancel if current thread also interrupted
            connectionExecutorService.shutdownNow()
            batchProcessingExecutorService.shutdownNow()
            // Preserve interrupt status
            Thread.currentThread().interrupt()
        }
    }
}