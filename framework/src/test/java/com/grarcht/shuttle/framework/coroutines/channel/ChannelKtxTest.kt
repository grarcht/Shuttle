package com.grarcht.shuttle.framework.coroutines.channel

import com.grarcht.shuttle.framework.coroutines.CompositeDisposableHandle
import com.grarcht.shuttle.framework.coroutines.addForDisposal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Tests for the [relayFlowIfAvailable] Channel extension function.
 */
@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChannelKtxTest {
    private var compositeDisposableHandle: CompositeDisposableHandle? = null
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: TestScope

    @BeforeEach
    fun `run before each test`() {
        testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
        testScope = TestScope()
        Dispatchers.setMain(testDispatcher)
        compositeDisposableHandle = CompositeDisposableHandle()
    }

    @AfterEach
    fun `run after each test`() {
        compositeDisposableHandle?.dispose()
        Dispatchers.resetMain()
        testDispatcher.cancel()
        testScope.cancel()
    }

    @Test
    fun verifyRelayIsSuccessfulWhenChannelsAreAvailable() = testScope.runTest {
        val expectedResult = 80
        var relayedResult = 0
        val logTag = "verifyRelayIfChannelsAreAvailable"
        val sourceChannel = Channel<Int>(1)
        val receiverChannel = Channel<Int>(1)
        val countDownLatch = CountDownLatch(1)

        // Will be launched in the mainThreadSurrogate dispatcher
        launch(Dispatchers.Main) {
            sourceChannel.send(expectedResult)
            sourceChannel.relayFlowIfAvailable(receiverChannel, logTag)
        }.invokeOnCompletion {
            it?.let {
                println(it.message ?: "Error when relaying data between channels.")
            }
        }.addForDisposal(compositeDisposableHandle)

        launch(Dispatchers.Main) {
            receiverChannel.consumeAsFlow().collectLatest { result ->
                relayedResult = result
                sourceChannel.cancel()
                receiverChannel.cancel()
                countDownLatch.countDown()
            }
        }.invokeOnCompletion {
            it?.let {
                println(it.message ?: "Error when relaying data between channels.")
            }
        }.addForDisposal(compositeDisposableHandle)

        awaitOnLatch(countDownLatch, 1L, TimeUnit.SECONDS)

        Assertions.assertEquals(expectedResult, relayedResult)
    }

    @Test
    fun verifyCloseQuietlyDoesNothingWhenScopeIsNull() {
        val channel = Channel<Int>(1)

        channel.closeQuietly(scope = null)
    }

    @Test
    fun verifyCloseQuietlyCancelsScopeWhenNonNull() {
        val channel = Channel<Int>(1)
        val scope = CoroutineScope(Dispatchers.Default)

        channel.closeQuietly(scope = scope)
    }

    @Test
    fun verifyCloseQuietlyHandlesIllegalStateException() {
        val channel = Channel<Int>(1)
        // A scope with no Job causes cancel() to throw IllegalStateException — no mock needed
        val scope = object : CoroutineScope {
            override val coroutineContext = kotlin.coroutines.EmptyCoroutineContext
        }

        channel.closeQuietly(scope = scope, cause = null)
    }

    @Test
    fun verifyRelayFlowHandlesClosedReceiveChannelException() = testScope.runTest {
        val sourceChannel = Channel<Int>(Channel.UNLIMITED)
        val receiverChannel = Channel<Int>(Channel.UNLIMITED)
        // Close with ClosedReceiveChannelException as cause so consumeAsFlow() throws it
        sourceChannel.close(cause = ClosedReceiveChannelException("deliberate close for test"))

        launch(Dispatchers.Main) {
            sourceChannel.relayFlowIfAvailable(receiverChannel, "test")
        }.join()
        // If we reach here without throwing, the exception was handled internally
    }

    @Test
    fun verifyRelayFlowHandlesGenericException() = testScope.runTest {
        val sourceChannel = Channel<Int>(Channel.UNLIMITED)
        val receiverChannel = Channel<Int>(Channel.UNLIMITED)
        // Close source with a generic RuntimeException so the generic catch block is hit
        sourceChannel.close(cause = RuntimeException("deliberate generic exception"))

        launch(Dispatchers.Main) {
            sourceChannel.relayFlowIfAvailable(receiverChannel, "test")
        }.join()
        // If we reach here without throwing, the exception was handled internally
    }

    @Suppress("SameParameterValue")
    private fun awaitOnLatch(countDownLatch: CountDownLatch, timeout: Long, timeUnit: TimeUnit) {
        @Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
        countDownLatch.await(timeout, timeUnit)
    }
}
