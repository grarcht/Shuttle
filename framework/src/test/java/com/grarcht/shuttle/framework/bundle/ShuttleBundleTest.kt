package com.grarcht.shuttle.framework.bundle

import android.os.Bundle
import android.os.Parcelable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LifecycleOwner
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
import org.junit.Rule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
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
    private var doesBundleMatch = false

    @Volatile
    private var resultBundle: Bundle? = null

    @BeforeAll
    fun runBeforeAllTests() {
        //https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
        Dispatchers.setMain(mainThreadSurrogate)

        val lifecycleOwner = mock(LifecycleOwner::class.java)
        val shuttleScreenFacade = mock(ShuttleFacade::class.java)

        shuttle = spy(CargoShuttle(shuttleScreenFacade, shuttleWarehouse, lifecycleOwner))
        doesBundleMatch = false
    }

    @AfterAll
    fun tearDown() {
        disposableHandle?.dispose()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
        testScope?.cancel()
    }


    @Test
    fun verifyPutAndGetBundle() {
        // ====== given ======
        var countDownLatch = CountDownLatch(1)
        val nestedBundleKey = "nestedBundle"
        val paintColorKey = "paint color"
        val map = mutableMapOf<String?, Any?>(Pair(paintColorKey, "blue"))
        val bundleToCreateFrom = MockBundleFactory().create(map)
        val shuttleBundle = ShuttleBundle.with(bundleToCreateFrom, shuttleWarehouse)
        val cargoId = "cargo id"

        // when
        shuttleBundle.transport(nestedBundleKey, bundleToCreateFrom)
        val bundle = shuttleBundle.create()
        countDownLatch.await(1, TimeUnit.SECONDS)
        countDownLatch = CountDownLatch(1)


        // verify
        runBlocking {
            testScope = this

            // Will be launched in the mainThreadSurrogate dispatcher
            disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttlePickupCargoResult> = shuttle.pickupCargo(
                    cargoId = cargoId,
                    creator = MockBundleFactory.creator
                )
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            ShuttlePickupCargoResult.Loading -> {
                                /* ignore */
                            }
                            is ShuttlePickupCargoResult.Success<*> -> {
                                resultBundle = shuttleResult.data as Bundle

                                if (resultBundle?.containsKey(paintColorKey) == true) {
                                    doesBundleMatch = true
                                    println("The key $nestedBundleKey was saved")
                                    channel.cancel()
                                } else {
                                    fail(IllegalStateException("The key wasn't saved"))
                                }
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
                    println(it.message ?: "Error when getting bundle.")
                }
            }
        }

        assertEquals(1, shuttleWarehouse.numberOfSaveInvocations)
        countDownLatch.await(1, TimeUnit.SECONDS)
        assertTrue(doesBundleMatch)
    }

    class TestRepository : ShuttleDataWarehouse() {
        private val pickupCargoChannel = Channel<ShuttlePickupCargoResult>(3)
        private val storeCargoChannel = Channel<ShuttleStoreCargoResult>(0)
        private val removeCargoChannel = Channel<ShuttleRemoveCargoResult>(0)

        override suspend fun <D : Parcelable> pickup(
            cargoId: String,
            parcelableCreator: Parcelable.Creator<D>,
            lifecycleOwner: LifecycleOwner
        ): Channel<ShuttlePickupCargoResult> {
            val parcelable = parcelableToEmit
            println("parcelableToEmit $parcelable")
            try {
                pickupCargoChannel.send(ShuttlePickupCargoResult.Success(parcelable))
            } catch (e: Exception) {
                println("caught: $e")
            }
            return pickupCargoChannel
        }

        override suspend fun <D : Parcelable> store(cargoId: String, data: D?): Channel<ShuttleStoreCargoResult> {
            super.store(cargoId, data)
            parcelableToEmit = data as Parcelable
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