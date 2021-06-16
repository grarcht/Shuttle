package com.grarcht.shuttle.framework

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.grarcht.shuttle.framework.bundle.MockBundleFactory
import com.grarcht.shuttle.framework.coroutines.CompositeDisposableHandle
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModel
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttleFileSystemGateway
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleCargoFacade
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleDataWarehouse
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.spy
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
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.io.File
import java.io.Serializable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val CARGO_FILE_PATH = "/cargo"
private const val STORE_CARGO_FAILURE = -1L
private const val STORE_CARGO_SUCCESS = 1L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CargoShuttleTests {
    private var compositeDisposableHandle: CompositeDisposableHandle? = null

    @OptIn(ObsoleteCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Volatile
    private var resultSerializable: Serializable? = null

    private var shuttle: Shuttle? = null
    private val shuttleScreenFacade = mock(ShuttleFacade::class.java)
    private val shuttleWarehouse = ShuttleDataWarehouse()
    private var testScope: CoroutineScope? = null

    @ExperimentalCoroutinesApi // This is only for the call to Dispatchers.setMain
    @BeforeAll
    fun runBeforeAllTests() {
        //https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
        Dispatchers.setMain(mainThreadSurrogate)
        shuttle = Mockito.spy(CargoShuttle(shuttleScreenFacade, shuttleWarehouse))
        compositeDisposableHandle = CompositeDisposableHandle()
    }

    @ExperimentalCoroutinesApi // This is only for the call to Dispatchers.resetMain
    @AfterAll
    fun tearDown() {
        runBlocking {
            shuttleWarehouse.removeAllCargo()
        }
        compositeDisposableHandle?.dispose()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
        testScope?.cancel()
        File(CARGO_FILE_PATH).deleteRecursively()
    }

    @Test
    fun verifyBundleCargoWithBundleReturnsAShuttleBundle() {
        val bundleFactory = MockBundleFactory()
        val bundle = bundleFactory.create()
        val shuttleBundle = shuttle?.bundleCargoWith(bundle, bundleFactory)
        Assertions.assertNotNull(shuttleBundle)
    }

    @Test
    fun verifyIntentCargoWithIntentReturnsAShuttleIntent() {
        val intent = Intent()
        val shuttleIntent = shuttle?.intentCargoWith(intent)
        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentCargoWithActionReturnsAShuttleIntent() {
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val shuttleIntent = shuttle?.intentCargoWith(intent)
        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentCargoWithActionAndUriReturnsAShuttleIntent() {
        val uri = mock(Uri::class.java)
        val shuttleIntent = shuttle?.intentCargoWith(Intent.ACTION_MEDIA_BUTTON, uri)
        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentCargoWithContextAndClassReturnsAShuttleIntent() {
        val context = mock(Context::class.java)
        val shuttleIntent = shuttle?.intentCargoWith(context, TestActivity::class.java)
        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentCargoWithActionUriContextAndClassReturnsAShuttleIntent() {
        val context = mock(Context::class.java)
        val uri = mock(Uri::class.java)
        val shuttleIntent = shuttle?.intentCargoWith(
            Intent.ACTION_MEDIA_BUTTON,
            uri,
            context,
            TestActivity::class.java
        )
        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentChooserCargoWithTargetAndTitleReturnsAShuttleIntent() {
        val title = "Test Title"
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val shuttleIntent = shuttle?.intentChooserCargoWith(intent, title)
        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentChooserCargoWithTargetTitleAndSenderReturnsAShuttleIntent() {
        val title = "Test Title"
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val sender = mock(IntentSender::class.java)
        val shuttleIntent = shuttle?.intentChooserCargoWith(intent, title, sender)
        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyPickupCargoSucceedsWithSuccessfulStore() {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val filePath = "/cargo/$cargoId"
        val countDownLatch = CountDownLatch(2)
        var storedId = ""

        runBlocking {
            testScope = this

            `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)
            `when`(dao.getCargoBy(cargoId)).thenReturn(CargoDataModel(cargoId, filePath))

            shuttle
                ?.intentCargoWith(Intent.ACTION_MEDIA_BUTTON)
                ?.transport(cargoId, cargo)
                ?.create()

            countDownLatch.await(1, TimeUnit.SECONDS)

            val disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttlePickupCargoResult>? = shuttle?.pickupCargo<Cargo>(cargoId)
                channel
                    ?.consumeAsFlow()
                    ?.collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttlePickupCargoResult.Loading -> {
                                /* ignore */
                            }
                            is ShuttlePickupCargoResult.Success<*> -> {
                                storedId = (shuttleResult.data as Cargo).cargoId
                                channel.cancel()
                                countDownLatch.countDown()
                            }
                            is ShuttlePickupCargoResult.Error<*> -> {
                                channel.cancel()
                                countDownLatch.countDown()
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
        Assertions.assertEquals(cargoId, storedId)
    }

    @Test
    fun verifyCargoRemovalOnReturnToScreen() {
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(3)
        val firstScreenClass = TestActivity().javaClass
        val nextScreen = TestActivity2()
        val nextScreenClass = nextScreen.javaClass
        val application = mock(Application::class.java)
        val warehouse = ShuttleDataWarehouse()
        val handler = mock(Handler::class.java)
        val facade = spy(ShuttleCargoFacade(application, warehouse, handler))
        val screenCallback = spy(facade.screenCallback)
        val cargoShuttle = CargoShuttle(facade, warehouse)
        val noCargo = "no cargo"
        var storeId = ""
        var channel: Channel<ShuttlePickupCargoResult>? = null

        runBlocking {
            testScope = this

            doAnswer {
                val runnable = it.getArgument(0, Runnable::class.java)
                runnable?.run()
                true
            }.`when`(handler).post(Mockito.any())

            cargoShuttle.cleanShuttleOnReturnTo(firstScreenClass, nextScreenClass, cargoId)

            cargoShuttle
                .intentCargoWith(Intent.ACTION_MEDIA_BUTTON)
                .transport(cargoId, cargo)

            countDownLatch.await(1, TimeUnit.SECONDS)
            screenCallback.onActivityCreated(nextScreen)
            nextScreen.onBackPressed()
            countDownLatch.await(1, TimeUnit.SECONDS)

            val disposableHandle = launch(Dispatchers.Main) {
                channel = cargoShuttle.pickupCargo<Cargo>(cargoId)
                channel
                    ?.consumeAsFlow()
                    ?.collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttlePickupCargoResult.Loading -> {
                                /* ignore */
                            }
                            is ShuttlePickupCargoResult.Success<*> -> {
                                countDownLatch.countDown()
                                cancel()
                            }
                            is ShuttlePickupCargoResult.Error<*> -> {
                                storeId = noCargo
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
        Assertions.assertEquals(noCargo, storeId)
    }

    @Test
    fun verifyCargoRemovalOnCleanShuttleFromDeliveryFor() {
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(3)
        val application = mock(Application::class.java)
        val warehouse = ShuttleDataWarehouse()
        val handler = mock(Handler::class.java)
        val facade = spy(ShuttleCargoFacade(application, warehouse, handler))
        val cargoShuttle = CargoShuttle(facade, warehouse)
        val noCargo = "no cargo"
        var storeId = ""
        var channel: Channel<ShuttlePickupCargoResult>? = null
        var numberOfValidSteps = 0

        cargoShuttle
            .intentCargoWith(Intent.ACTION_MEDIA_BUTTON)
            .transport(cargoId, cargo)

        countDownLatch.await(1, TimeUnit.SECONDS)

        runBlocking {
            testScope = this

            doAnswer {
                val runnable = it.getArgument(0, Runnable::class.java)
                runnable?.run()
                true
            }.`when`(handler).post(Mockito.any())

            val receiverChannel = Channel<ShuttleRemoveCargoResult>()
            val disposableHandle = launch(Dispatchers.Main) {
                receiverChannel
                    .consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttleRemoveCargoResult.DoesNotExist -> {
                                cancel()
                            }
                            is ShuttleRemoveCargoResult.Removed -> {
                                numberOfValidSteps++
                                cancel()
                            }
                            is ShuttleRemoveCargoResult.Removing -> {
                                numberOfValidSteps++
                            }
                            is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
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

            cargoShuttle.cleanShuttleFromDeliveryFor(cargoId, receiverChannel)

            val disposableHandle2 = launch(Dispatchers.Main) {
                channel = cargoShuttle.pickupCargo<Cargo>(cargoId)
                channel
                    ?.consumeAsFlow()
                    ?.collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttlePickupCargoResult.Loading -> {
                                /* ignore */
                            }
                            is ShuttlePickupCargoResult.Success<*> -> {
                                countDownLatch.countDown()
                                cancel()
                            }
                            is ShuttlePickupCargoResult.Error<*> -> {
                                storeId = noCargo
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
            compositeDisposableHandle?.add(disposableHandle2)

        }

        countDownLatch.await(1, TimeUnit.SECONDS)
        Assertions.assertEquals(noCargo, storeId)
        Assertions.assertEquals(2, numberOfValidSteps)
    }

    @Test
    fun verifyCargoRemovalOnCleanShuttleFromAllDeliveries() {
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(3)
        val application = mock(Application::class.java)
        val warehouse = ShuttleDataWarehouse()
        val handler = mock(Handler::class.java)
        val facade = spy(ShuttleCargoFacade(application, warehouse, handler))
        val cargoShuttle = CargoShuttle(facade, warehouse)
        val noCargo = "no cargo"
        var storeId = ""
        var channel: Channel<ShuttlePickupCargoResult>? = null
        var numberOfValidSteps = 0

        cargoShuttle
            .intentCargoWith(Intent.ACTION_MEDIA_BUTTON)
            .transport(cargoId, cargo)

        countDownLatch.await(1, TimeUnit.SECONDS)

        runBlocking {
            testScope = this

            doAnswer {
                val runnable = it.getArgument(0, Runnable::class.java)
                runnable?.run()
                true
            }.`when`(handler).post(Mockito.any())

            val receiverChannel = Channel<ShuttleRemoveCargoResult>()
            val disposableHandle = launch(Dispatchers.Main) {
                receiverChannel
                    .consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttleRemoveCargoResult.DoesNotExist -> {
                                cancel()
                            }
                            is ShuttleRemoveCargoResult.Removed -> {
                                numberOfValidSteps++
                                cancel()
                            }
                            is ShuttleRemoveCargoResult.Removing -> {
                                numberOfValidSteps++
                            }
                            is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
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

            cargoShuttle.cleanShuttleFromAllDeliveries(receiverChannel)

            val disposableHandle2 = launch(Dispatchers.Main) {
                channel = cargoShuttle.pickupCargo<Cargo>(cargoId)
                channel
                    ?.consumeAsFlow()
                    ?.collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttlePickupCargoResult.Loading -> {
                                /* ignore */
                            }
                            is ShuttlePickupCargoResult.Success<*> -> {
                                countDownLatch.countDown()
                                cancel()
                            }
                            is ShuttlePickupCargoResult.Error<*> -> {
                                storeId = noCargo
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
            compositeDisposableHandle?.add(disposableHandle2)

        }

        countDownLatch.await(1, TimeUnit.SECONDS)
        Assertions.assertEquals(noCargo, storeId)
        Assertions.assertEquals(2, numberOfValidSteps)
    }

    private data class CargoDataModel(override val cargoId: String, override val filePath: String) : ShuttleDataModel
    private data class Cargo(val cargoId: String, val numberOfBoxes: Int) : Serializable
    private class TestActivity : AppCompatActivity()
    private class TestActivity2 : AppCompatActivity()
}