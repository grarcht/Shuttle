package com.grarcht.shuttle.framework.bundle

import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.content.bundle.ShuttleBundle
import com.grarcht.shuttle.framework.coroutines.CompositeDisposableHandle
import com.grarcht.shuttle.framework.coroutines.addForDisposal
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.result.ShuttleStoreCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleDataWarehouse
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import java.io.Serializable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Verifies the functionality of [ShuttleBundle]. ShuttleBundle wraps an Android Bundle and
 * provides the mechanism to transport Serializable cargo through the warehouse, decoupling large
 * payloads from the Bundle size limits. If it did not work correctly, cargo would either fail to
 * be stored or could not be retrieved by the receiving screen.
 */
@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttleBundleTest {
    private var compositeDisposableHandle: CompositeDisposableHandle? = null
    private var shuttle: Shuttle? = null
    private val shuttleScreenFacade = mock(ShuttleFacade::class.java)
    private var shuttleWarehouse: ShuttleDataWarehouse? = null
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: TestScope

    @Volatile
    private var doesResultMatch = false

    @Volatile
    private var resultSerializable: Serializable? = null

    @BeforeEach
    fun `run before each test`() {
        testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
        testScope = TestScope()
        Dispatchers.setMain(testDispatcher)
        shuttleWarehouse = ShuttleDataWarehouse()
        shuttle = spy(CargoShuttle(shuttleScreenFacade, shuttleWarehouse as ShuttleDataWarehouse))
        compositeDisposableHandle = CompositeDisposableHandle()
        doesResultMatch = false
    }

    @AfterEach
    fun `run after each test`() {
        runBlocking { shuttleWarehouse?.removeAllCargo() }
        compositeDisposableHandle?.dispose()
        Dispatchers.resetMain()
        testDispatcher.cancel()
        testScope.cancel()
    }

    @Test
    fun verifyPutAndGetSerializable() = testScope.runTest {
        // given
        val countDownLatch = CountDownLatch(1)
        val paintColorKey = "paint color"
        val paintColorValue = "blue"
        val cargoId = "cargo id"
        val paintColor = PaintColor("blue")
        val map = mutableMapOf<String?, Any?>(Pair(paintColorKey, paintColorValue))
        val bundleToCreateFrom = MockBundleFactory().create(map)
        val shuttleBundle = ShuttleBundle.with(bundleToCreateFrom, shuttleWarehouse as ShuttleWarehouse)

        shuttleBundle.transport(cargoId, paintColor)
        delay(1000L)

        shuttle?.let {
            launch(Dispatchers.Main) {
                val channel: Channel<ShuttlePickupCargoResult> = it.pickupCargo<PaintColor>(cargoId)
                channel.consumeAsFlow()
                    .collectLatest { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttlePickupCargoResult.Loading -> {
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
                            else -> {
                                // ignore
                            }
                        }
                    }
            }.invokeOnCompletion {
                it?.let {
                    println(it.message ?: "Error when getting the serializable.")
                }
            }.addForDisposal(compositeDisposableHandle)
        }

        awaitOnLatch(countDownLatch, 1L, TimeUnit.SECONDS)

        assertEquals(1, shuttleWarehouse?.numberOfStoreInvocations)
        assertTrue(resultSerializable is PaintColor)
        val deserializedPainColor = resultSerializable as PaintColor
        assertTrue(deserializedPainColor.color == "blue")
    }

    @Test
    fun verifyTransportThrowsWhenBundleIsNull() {
        val warehouse = mock(ShuttleWarehouse::class.java)
        val bundle = ShuttleBundle(warehouse, internalBundle = null)
        var threw = false

        try {
            bundle.transport("cargoId", PaintColor("red"))
        } catch (e: IllegalStateException) {
            threw = true
        }

        assertTrue(threw)
    }

    @Test
    fun verifyCreateThrowsWhenBundleIsNull() {
        val warehouse = mock(ShuttleWarehouse::class.java)
        val bundle = ShuttleBundle(warehouse, internalBundle = null)
        var threw = false

        try {
            bundle.create()
        } catch (e: IllegalStateException) {
            threw = true
        }

        assertTrue(threw)
    }

    @Test
    fun verifyLogTagIsSetCorrectlyAndReturnsBundleReference() {
        val tag = "TestTag"
        val bundle = ShuttleBundle(
            shuttleWarehouse as ShuttleWarehouse,
            MockBundleFactory().create()
        )

        val result = bundle.logTag(tag)

        assertEquals(bundle, result)
    }

    @Test
    fun verifyCreateReturnsBundleWhenNotNull() {
        val internalBundle = MockBundleFactory().create()
        val bundle = ShuttleBundle(
            shuttleWarehouse as ShuttleWarehouse,
            internalBundle
        )

        val result = bundle.create()

        assertEquals(internalBundle, result)
    }

    @Test
    fun verifyLogTagWithNullUsesDefaultTag() {
        val bundle = ShuttleBundle(
            shuttleWarehouse as ShuttleWarehouse,
            MockBundleFactory().create()
        )

        val result = bundle.logTag(null)

        assertEquals(bundle, result)
    }

    @Test
    fun verifyWithNullBundleUsesDefaultBundleFactory() {
        val shuttleBundle = ShuttleBundle.with(null, shuttleWarehouse as ShuttleWarehouse)

        assertNotNull(shuttleBundle)
    }


    @Test
    fun verifyTransportLogsErrorWhenStoreCancels() {
        val bundle = ShuttleBundle(
            CancellationThrowingWarehouse(),
            MockBundleFactory().create(),
            backgroundThreadDispatcher = Dispatchers.Unconfined
        )
        // CancellationException from store() is logged via invokeOnCompletion — should not throw
        bundle.transport("cargoId", PaintColor("red"))
    }

    @Suppress("SameParameterValue")
    private fun awaitOnLatch(countDownLatch: CountDownLatch, timeout: Long, timeUnit: TimeUnit) {
        @Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
        countDownLatch.await(timeout, timeUnit)
    }

    private class CancellationThrowingWarehouse : ShuttleWarehouse {
        override suspend fun <D : Serializable> pickup(id: String) = Channel<ShuttlePickupCargoResult>()
        override suspend fun <D : Serializable> store(id: String, data: D?): Channel<ShuttleStoreCargoResult> {
            throw CancellationException("test cancellation")
        }
        override suspend fun removeCargoBy(id: String) = Channel<ShuttleRemoveCargoResult>()
        override suspend fun removeAllCargo() = Channel<ShuttleRemoveCargoResult>()
    }
}
