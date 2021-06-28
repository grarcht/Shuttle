package com.grarcht.shuttle.framework.bundle

import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.content.bundle.ShuttleBundle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleDataWarehouse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import java.io.Serializable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttleBundleTest {
    @OptIn(ObsoleteCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private val shuttleWarehouse = ShuttleDataWarehouse()
    private var testScope: CoroutineScope? = null
    private var disposableHandle: DisposableHandle? = null
    private lateinit var shuttle: Shuttle

    @Volatile
    private var doesResultMatch = false

    @Volatile
    private var resultSerializable: Serializable? = null

    @ExperimentalCoroutinesApi // This is only for the call to Dispatchers.setMain
    @BeforeAll
    fun runBeforeAllTests() {
        //https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
        Dispatchers.setMain(mainThreadSurrogate)

        val shuttleScreenFacade = mock(ShuttleFacade::class.java)

        shuttle = spy(CargoShuttle(shuttleScreenFacade, shuttleWarehouse))
        doesResultMatch = false
    }

    @ExperimentalCoroutinesApi // This is only for the call to Dispatchers.resetMain
    @AfterAll
    fun tearDown() {
        disposableHandle?.dispose()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
        testScope?.cancel()
    }


    @Test
    fun verifyPutAndGetSerializable() {
        // given
        var countDownLatch = CountDownLatch(1)
        val paintColorKey = "paint color"
        val paintColorValue = "blue"
        val cargoId = "cargo id"

        class PaintColor(val color: String?) : Serializable

        val paintColor = PaintColor("blue")
        val map = mutableMapOf<String?, Any?>(Pair(paintColorKey, paintColorValue))
        val bundleToCreateFrom = MockBundleFactory().create(map)
        val shuttleBundle = ShuttleBundle.with(bundleToCreateFrom, shuttleWarehouse)

        // when
        shuttleBundle.transport(cargoId, paintColor)
        countDownLatch.await(1, TimeUnit.SECONDS)
        countDownLatch = CountDownLatch(1)

        // verify
        runBlocking {
            testScope = this

            // Will be launched in the mainThreadSurrogate dispatcher
            disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttlePickupCargoResult> = shuttle.pickupCargo<PaintColor>(cargoId)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            ShuttlePickupCargoResult.Loading -> {
                                /* ignore */
                            }
                            is ShuttlePickupCargoResult.Success<*> -> {
                                resultSerializable = shuttleResult.data as Serializable
                                channel.cancel()
                                countDownLatch.countDown()
                            }
                            is ShuttlePickupCargoResult.Error<*> -> {
                                countDownLatch.countDown()
                                fail()
                            }
                        }
                    }
            }.invokeOnCompletion {
                it?.let {
                    println(it.message ?: "Error when getting the serializable.")
                }
            }
        }

        assertEquals(1, shuttleWarehouse.numberOfStoreInvocations)
        countDownLatch.await(1, TimeUnit.SECONDS)
        Assertions.assertTrue(resultSerializable is PaintColor)
        val deserializedPainColor = resultSerializable as PaintColor
        Assertions.assertTrue(deserializedPainColor.color == "blue")
    }
}
