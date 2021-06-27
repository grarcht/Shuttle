package com.grarcht.shuttle.framework.addons.navigation

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.addons.bundle.MockBundleFactory
import com.grarcht.shuttle.framework.addons.coroutines.CompositeDisposableHandle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.addons.warehouse.ShuttleDataWarehouse
import com.nhaarman.mockitokotlin2.doNothing
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
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import java.io.Serializable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val ACTION_ID = 5000
private val ARGUMENTS = Bundle.EMPTY

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttleNavControllerTests {
    private var compositeDisposableHandle: CompositeDisposableHandle? = null
    private var navController = mock(NavController::class.java)
    private var testScope: CoroutineScope? = null

    @OptIn(ObsoleteCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @ExperimentalCoroutinesApi // This is only for the call to Dispatchers.setMain
    @BeforeAll
    fun runBeforeAllTests() {
        //https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
        Dispatchers.setMain(mainThreadSurrogate)
        compositeDisposableHandle = CompositeDisposableHandle()
    }

    @ExperimentalCoroutinesApi // This is only for the call to Dispatchers.resetMain
    @AfterAll
    fun runAfterAllTests() {
        compositeDisposableHandle?.dispose()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
        testScope?.cancel()
    }

    @Test
    fun verifyNavigateWithNavDirections() {
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 50)
        val countDownLatch = CountDownLatch(1)
        val directions = TestNavDirections()
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val cargoShuttle = CargoShuttle(shuttleFacade, shuttleWarehouse)
        var channel: Channel<ShuttlePickupCargoResult>? = null
        var numberOfValidSteps = 0
        var storeId = ""

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

        // pickup the cargo
        runBlocking {
            testScope = this

            val disposableHandle = launch(Dispatchers.Main) {
                channel = cargoShuttle.pickupCargo<Cargo>(cargoId)
                channel
                    ?.consumeAsFlow()
                    ?.collect { shuttleResult ->
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
                        }
                    }
            }.invokeOnCompletion {
                it?.let {
                    println(it.message ?: "Error when getting the serializable.")
                }
            }
            compositeDisposableHandle?.add(disposableHandle)
        }

        countDownLatch.await(1, TimeUnit.SECONDS)
        Assertions.assertEquals(cargoId, storeId)
        Assertions.assertEquals(2, numberOfValidSteps)
        cargoShuttle.cleanShuttleFromAllDeliveries()
    }

    @Test
    fun verifyNavigateWithNavId() {
        val cargoId = "cargoId2"
        val cargo = Cargo(cargoId, 150)
        val countDownLatch = CountDownLatch(1)
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val shuttleWarehouse = ShuttleDataWarehouse()
        val cargoShuttle = CargoShuttle(shuttleFacade, shuttleWarehouse)
        var channel: Channel<ShuttlePickupCargoResult>? = null
        var numberOfValidSteps = 0
        var storeId = ""

        // Navigate to the destination
        val shuttleNavController = ShuttleNavController.navigateWith(
            shuttleWarehouse,
            shuttleFacade,
            navController,
            R.id.nav_host_fragment_container, // used since it's a res id.  For the test, it doesn't matter.
            bundleFactory = MockBundleFactory()
        )
        // The guts of function cleanShuttleOnReturnTo are tested in the framework module's tests.
        shuttleNavController
            .transport(cargoId, cargo)
            .deliver()

        // pickup the cargo
        runBlocking {
           // testScope = this

            val disposableHandle = launch(Dispatchers.Main) {
                channel = cargoShuttle.pickupCargo<Cargo>(cargoId)
                channel
                    ?.consumeAsFlow()
                    ?.collect { shuttleResult ->
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
                        }
                    }
            }.invokeOnCompletion {
                it?.let {
                    println(it.message ?: "Error when getting the serializable.")
                }
            }
            compositeDisposableHandle?.add(disposableHandle)
        }

        countDownLatch.await(1, TimeUnit.SECONDS)
        Assertions.assertEquals(cargoId, storeId)
        Assertions.assertEquals(2, numberOfValidSteps)
    }

    private data class Cargo(val cargoId: String, val numberOfBoxes: Int) : Serializable

    private class TestNavDirections() : NavDirections {
        override fun getActionId(): Int = ACTION_ID
        override fun getArguments(): Bundle = ARGUMENTS
    }
}