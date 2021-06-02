package com.grarcht.shuttle.framework.coroutines.channel

import com.grarcht.shuttle.framework.coroutines.CompositeDisposableHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Tests for the [relayFlowIfAvailable] Channel extension function.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChannelKtxTest {
    private val compositeDisposableHandle = CompositeDisposableHandle()

    @OptIn(ObsoleteCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private var testScope: CoroutineScope? = null

    @ExperimentalCoroutinesApi // This is only for the call to Dispatchers.setMain
    @BeforeAll
    fun runBeforeAllTests() {
        //https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @ExperimentalCoroutinesApi // This is only for the call to Dispatchers.resetMain
    @AfterAll
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
        compositeDisposableHandle.dispose()
        testScope?.cancel()
    }

    @Test
    fun verifyRelayIsSuccessfulWhenChannelsAreAvailable() {
        val expectedResult = 80
        var relayedResult = 0

        val logTag = "verifyRelayIfChannelsAreAvailable"

        runBlocking {
            testScope = this
            val sourceChannel = Channel<Int>(1)
            val receiverChannel = Channel<Int>(1)

            // Will be launched in the mainThreadSurrogate dispatcher
            val disposableHandle = launch(Dispatchers.Main) {
                sourceChannel.send(expectedResult)
                sourceChannel.relayFlowIfAvailable(receiverChannel, logTag)
            }.invokeOnCompletion {
                it?.let {
                    println(it.message ?: "Error when relaying data between channels.")
                }
            }
            compositeDisposableHandle.add(disposableHandle)

            val disposableHandle2 = launch(Dispatchers.Main) {
                receiverChannel.consumeAsFlow().collect { result ->
                    relayedResult = result
                    sourceChannel.cancel()
                    receiverChannel.cancel()
                }
            }.invokeOnCompletion {
                it?.let {
                    println(it.message ?: "Error when relaying data between channels.")
                }
            }
            compositeDisposableHandle.add(disposableHandle2)
        }

        Assertions.assertEquals(expectedResult, relayedResult)
    }
}