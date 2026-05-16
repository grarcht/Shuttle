package com.grarcht.shuttle.framework.addons.navigation

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.addons.ArchtTestTaskExecutorExtension
import com.grarcht.shuttle.framework.addons.bundle.MockBundleFactory
import com.grarcht.shuttle.framework.addons.coroutines.CompositeDisposableHandle
import com.grarcht.shuttle.framework.addons.warehouse.ShuttleDataWarehouse
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleStoreCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleFacade
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
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val ACTION_ID = 5000
private val ARGUMENTS = Bundle()

/**
 * Verifies the functionality of [ShuttleNavController]. ShuttleNavController is the Navigation
 * Component adapter that packages cargo transport and Jetpack Navigation into a single fluent
 * API, storing payloads in the warehouse before triggering the navigation action. Without it,
 * Navigation-driven screens would have no way to pass large data objects to their destinations.
 */
@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ArchtTestTaskExecutorExtension::class)
class ShuttleNavControllerTests {
    private var compositeDisposableHandle: CompositeDisposableHandle? = null
    private var navController = mock(NavController::class.java)
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
    fun verifyNavigateWithNavDirections() = testScope.runTest {
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 50)
        val directions = TestNavDirections()
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val cargoShuttle = CargoShuttle(shuttleFacade, shuttleWarehouse)
        var channel: Channel<ShuttlePickupCargoResult>?
        var numberOfValidSteps = 0
        var storeId = ""
        val countDownLatch = CountDownLatch(2)

        doNothing().`when`(navController).navigate(directions)

        // Navigate to the destination
        val shuttleNavController = ShuttleNavController.navigateWith(
            shuttleWarehouse,
            shuttleFacade,
            navController,
            directions,
            bundleFactory = MockBundleFactory()
        )
        // The guts of function cleanShuttleOnReturnTo are tested in the framework module's tests.
        shuttleNavController
            .transport(cargoId, cargo)
            .deliver()

        awaitOnLatch(countDownLatch, 1, TimeUnit.SECONDS)

        // pickup the cargo
        val disposableHandle = launch(Dispatchers.Main) {
            channel = cargoShuttle.pickupCargo<Cargo>(cargoId)
            channel
                ?.consumeAsFlow()
                ?.collectLatest { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttlePickupCargoResult.Loading -> {
                            numberOfValidSteps++
                        }
                        is ShuttlePickupCargoResult.Success<*> -> {
                            storeId = (shuttleResult.data as Cargo).cargoId
                            numberOfValidSteps++
                            countDownLatch.countDown()
                            cancel()
                        }
                        is ShuttlePickupCargoResult.Error<*> -> {
                            countDownLatch.countDown()
                            cancel()
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
        }
        compositeDisposableHandle?.add(disposableHandle)

        delay(1000L)
        advanceUntilIdle()

        assertAll(
            { Assertions.assertEquals(cargoId, storeId) },
            { Assertions.assertEquals(2, numberOfValidSteps) }
        )
        cargoShuttle.cleanShuttleFromAllDeliveries()
    }

    @Test
    fun verifyNavigateWithNavId() = testScope.runTest {
        val cargoId = "cargoId2"
        val cargo = Cargo(cargoId, 150)
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val cargoShuttle = CargoShuttle(shuttleFacade, shuttleWarehouse)
        var channel: Channel<ShuttlePickupCargoResult>?
        var numberOfValidSteps = 0
        var storeId = ""
        val countDownLatch = CountDownLatch(2)

        // Navigate to the destination
        val shuttleNavController = ShuttleNavController.navigateWith(
            shuttleWarehouse,
            shuttleFacade,
            navController,
            /* used since it's a res id.  For the test, it doesn't matter. */
            androidx.navigation.fragment.R.id.nav_host_fragment_container,
            bundleFactory = MockBundleFactory()
        )
        // The guts of function cleanShuttleOnReturnTo are tested in the framework module's tests.
        shuttleNavController
            .transport(cargoId, cargo)
            .deliver()

        awaitOnLatch(countDownLatch, 1, TimeUnit.SECONDS)

        val disposableHandle = launch(Dispatchers.Main) {
            channel = cargoShuttle.pickupCargo<Cargo>(cargoId)
            channel
                ?.consumeAsFlow()
                ?.collectLatest { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttlePickupCargoResult.Loading -> {
                            numberOfValidSteps++
                        }
                        is ShuttlePickupCargoResult.Success<*> -> {
                            storeId = (shuttleResult.data as Cargo).cargoId
                            numberOfValidSteps++
                            countDownLatch.countDown()
                            cancel()
                        }
                        is ShuttlePickupCargoResult.Error<*> -> {
                            countDownLatch.countDown()
                            cancel()
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
        }
        compositeDisposableHandle?.add(disposableHandle)

        awaitOnLatch(countDownLatch, 1, TimeUnit.SECONDS)

        assertAll(
            { Assertions.assertEquals(cargoId, storeId) },
            { Assertions.assertEquals(2, numberOfValidSteps) }
        )
    }

    @Test
    fun verifyLogTagSetsTag() {
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val controller = ShuttleNavController.navigateWith(
            shuttleWarehouse,
            shuttleFacade,
            navController,
            resId = androidx.navigation.fragment.R.id.nav_host_fragment_container,
            bundleFactory = MockBundleFactory(),
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        val result = controller.logTag("TestTag")
        Assertions.assertSame(controller, result)
    }

    @Test
    fun verifyLogTagWithNullUsesDefault() {
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val controller = ShuttleNavController.navigateWith(
            shuttleWarehouse,
            shuttleFacade,
            navController,
            resId = androidx.navigation.fragment.R.id.nav_host_fragment_container,
            bundleFactory = MockBundleFactory(),
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        val result = controller.logTag(null)
        Assertions.assertSame(controller, result)
    }

    @Test
    fun verifyDeliverWithNullStoreJobSkipsInvokeOnCompletion() {
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val controller = ShuttleNavController.navigateWith(
            shuttleWarehouse,
            shuttleFacade,
            navController,
            resId = androidx.navigation.fragment.R.id.nav_host_fragment_container,
            bundleFactory = MockBundleFactory(),
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        // transport with null serializable: puts parcelable in bundle (so bundle is non-empty),
        // but storeCargoJob remains null. deliver() should skip invokeOnCompletion.
        controller.transport("cargoNullSerial", null).deliver()
    }

    @Test
    fun verifyTransportWithNullSerializableDoesNotLaunchStoreJob() {
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val controller = ShuttleNavController.navigateWith(
            shuttleWarehouse,
            shuttleFacade,
            navController,
            resId = androidx.navigation.fragment.R.id.nav_host_fragment_container,
            bundleFactory = MockBundleFactory(),
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        val result = controller.transport("cargoNull", null)
        Assertions.assertSame(controller, result)
    }

    @Test
    fun verifyCleanShuttleOnReturnTo() {
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val controller = ShuttleNavController.navigateWith(
            shuttleWarehouse,
            shuttleFacade,
            navController,
            resId = androidx.navigation.fragment.R.id.nav_host_fragment_container,
            bundleFactory = MockBundleFactory(),
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        val result = controller.cleanShuttleOnReturnTo(
            ShuttleNavControllerTests::class.java,
            ShuttleNavControllerTests::class.java,
            "cargoId"
        )
        Assertions.assertSame(controller, result)
    }

    @Test
    fun verifyDeliverWithNullBundleLogsWarningAndSkipsNavigation() {
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val controller = ShuttleNavController(
            shuttleWarehouse = shuttleWarehouse,
            shuttleScreenFacade = shuttleFacade,
            navController = navController,
            internalBundle = null,
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        // In DEBUG builds, deliver() throws when internalBundle is null; covers internalBundle?.isEmpty ?: true null branch
        assertThrows<IllegalStateException> {
            controller.deliver()
        }
    }

    @Test
    fun verifyDeliverWithNullBundleAfterTransportCoversNullBundleBranch() {
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val directions = TestNavDirections()
        val controller = ShuttleNavController(
            shuttleWarehouse = shuttleWarehouse,
            shuttleScreenFacade = shuttleFacade,
            navController = navController,
            navDirections = directions,
            internalBundle = null,
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        val cargoId = "cargoNullBundle"
        controller.transport(cargoId, Cargo(cargoId, 1))
        // In DEBUG builds, deliver() throws when internalBundle is null; covers internalBundle?.isEmpty ?: true null branch
        assertThrows<IllegalStateException> {
            controller.deliver()
        }
    }

    @Test
    fun verifyNavigateWithNavDirectionsAndNavOptions() = testScope.runTest {
        val cargoId = "cargoNavOptions"
        val cargo = Cargo(cargoId, 10)
        val directions = TestNavDirections()
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val navOptions = androidx.navigation.NavOptions.Builder().build()

        val controller = ShuttleNavController.navigateWith(
            shuttleWarehouse,
            shuttleFacade,
            navController,
            navDirections = directions,
            navOptions = navOptions,
            bundleFactory = MockBundleFactory(),
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        controller.transport(cargoId, cargo).deliver()
        advanceUntilIdle()
    }

    @Test
    fun verifyNavigateWithNavDirectionsAndNavigatorExtras() = testScope.runTest {
        val cargoId = "cargoNavExtras"
        val cargo = Cargo(cargoId, 20)
        val directions = TestNavDirections()
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val navigatorExtras = mock(androidx.navigation.Navigator.Extras::class.java)

        val controller = ShuttleNavController.navigateWith(
            shuttleWarehouse,
            shuttleFacade,
            navController,
            navDirections = directions,
            navigatorExtras = navigatorExtras,
            bundleFactory = MockBundleFactory(),
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        controller.transport(cargoId, cargo).deliver()
        advanceUntilIdle()
    }

    @Test
    fun verifyNavigateWithNoDirectionsAndNoResId() = testScope.runTest {
        val cargoId = "cargoNoNav"
        val cargo = Cargo(cargoId, 30)
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val bundle = MockBundleFactory().create()

        val controller = ShuttleNavController(
            shuttleWarehouse = shuttleWarehouse,
            shuttleScreenFacade = shuttleFacade,
            navController = navController,
            navDirections = null,
            resId = null,
            internalBundle = bundle,
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        controller.transport(cargoId, cargo).deliver()
        advanceUntilIdle()
    }

    @Test
    fun verifyDeliverWithErrorInStoreLogsError() {
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val warehouse = CancellationThrowingWarehouse()
        val bundle = MockBundleFactory().create()
        val cargoId = "cargoError"
        val cargo = Cargo(cargoId, 40)

        val controller = ShuttleNavController(
            shuttleWarehouse = warehouse,
            shuttleScreenFacade = shuttleFacade,
            navController = navController,
            internalBundle = bundle,
            backgroundThreadDispatcher = Dispatchers.Unconfined,
            mainThreadDispatcher = Dispatchers.Unconfined
        )
        controller.transport(cargoId, cargo).deliver()
    }

    @Test
    fun verifyDefaultConstructorDispatchers() {
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val bundle = MockBundleFactory().create()
        // Constructs using default backgroundThreadDispatcher and mainThreadDispatcher
        val controller = ShuttleNavController(
            shuttleWarehouse = shuttleWarehouse,
            shuttleScreenFacade = shuttleFacade,
            navController = navController,
            internalBundle = bundle
        )
        Assertions.assertNotNull(controller)
    }

    @Suppress("SameParameterValue")
    private fun awaitOnLatch(countDownLatch: CountDownLatch, timeout: Long, timeUnit: TimeUnit) {
        @Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
        countDownLatch.await(timeout, timeUnit)
    }

    private class TestNavDirections(
        override val actionId: Int = ACTION_ID,
        override val arguments: Bundle = ARGUMENTS
    ) : NavDirections

    private class CancellationThrowingWarehouse : ShuttleWarehouse {
        override suspend fun <D : com.grarcht.shuttle.framework.ShuttleCargoData> pickup(cargoId: String): kotlinx.coroutines.channels.Channel<ShuttlePickupCargoResult> {
            throw CancellationException("pickup not supported")
        }

        override suspend fun <D : com.grarcht.shuttle.framework.ShuttleCargoData> store(cargoId: String, data: D?): kotlinx.coroutines.channels.Channel<ShuttleStoreCargoResult> {
            throw CancellationException("Simulated cancellation during store")
        }

        override suspend fun removeCargoBy(cargoId: String): kotlinx.coroutines.channels.Channel<com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult> {
            throw CancellationException("removeCargoBy not supported")
        }

        override suspend fun removeAllCargo(): kotlinx.coroutines.channels.Channel<com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult> {
            throw CancellationException("removeAllCargo not supported")
        }
    }
}
