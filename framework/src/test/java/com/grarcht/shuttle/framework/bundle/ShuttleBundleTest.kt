package com.grarcht.shuttle.framework.bundle

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.content.bundle.ShuttleBundle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.result.ShuttleStoreCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
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
import org.junit.Assert
import org.junit.Rule
import org.junit.jupiter.api.AfterAll
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
    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @OptIn(ObsoleteCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private val shuttleWarehouse = TestRepository()
    private var testScope: CoroutineScope? = null
    private var disposableHandle: DisposableHandle? = null
    private lateinit var shuttle: Shuttle

    @Volatile
    private var doesResultMatch = false

    @Volatile
    private var resultSerializable: Serializable? = null

    @BeforeAll
    fun runBeforeAllTests() {
        //https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
        Dispatchers.setMain(mainThreadSurrogate)

        val shuttleScreenFacade = mock(ShuttleFacade::class.java)

        shuttle = spy(CargoShuttle(shuttleScreenFacade, shuttleWarehouse))
        doesResultMatch = false
    }

    @AfterAll
    fun tearDown() {
        disposableHandle?.dispose()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
        testScope?.cancel()
    }


    @Test
    fun verifyPutAndGetSerializable() {
        // ====== given ======
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

        assertEquals(1, shuttleWarehouse.numberOfSaveInvocations)
        countDownLatch.await(1, TimeUnit.SECONDS)
        Assert.assertTrue(resultSerializable is PaintColor)
        val deserializedPainColor = resultSerializable as PaintColor
        Assert.assertTrue(deserializedPainColor.color == "blue")
    }

    class TestRepository : ShuttleDataWarehouse() {
        private val pickupCargoChannel = Channel<ShuttlePickupCargoResult>(3)
        private val storeCargoChannel = Channel<ShuttleStoreCargoResult>(0)
        private val removeCargoChannel = Channel<ShuttleRemoveCargoResult>(0)

        override suspend fun <D : Serializable> pickup(cargoId: String): Channel<ShuttlePickupCargoResult> {
            val parcelable = serializableToEmit
            println("parcelableToEmit $parcelable")
            try {
                pickupCargoChannel.send(ShuttlePickupCargoResult.Success(parcelable))
            } catch (e: Exception) {
                println("caught: $e")
            }
            return pickupCargoChannel
        }

        override suspend fun <D : Serializable> store(cargoId: String, data: D?): Channel<ShuttleStoreCargoResult> {
            super.store(cargoId, data)
            serializableToEmit = data as Serializable
            return storeCargoChannel
        }

        override suspend fun removeCargoBy(cargoId: String): Channel<ShuttleRemoveCargoResult> {
            return removeCargoChannel
        }

        override suspend fun removeAllCargo(): Channel<ShuttleRemoveCargoResult> {
            return removeCargoChannel
        }
    }
}