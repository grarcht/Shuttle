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
import com.grarcht.shuttle.framework.coroutines.addForDisposal
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModel
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttleFileSystemGateway
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleCargoFacade
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleDataWarehouse
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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.kotlin.spy
import java.io.File
import java.io.Serializable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val CARGO_FILE_PATH = "/cargo"

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ArchtTestTaskExecutorExtension::class)
class CargoShuttleTests {

    private var compositeDisposableHandle: CompositeDisposableHandle? = null
    private var shuttle: Shuttle? = null
    private val shuttleScreenFacade = mock(ShuttleFacade::class.java)
    private var shuttleWarehouse: ShuttleDataWarehouse? = null
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: TestScope

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @BeforeEach
    fun `run before each test`() {
        testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
        testScope = TestScope()
        Dispatchers.setMain(testDispatcher)
        shuttleWarehouse = ShuttleDataWarehouse()
        shuttle = Mockito.spy(CargoShuttle(shuttleScreenFacade, shuttleWarehouse as ShuttleDataWarehouse))
        compositeDisposableHandle = CompositeDisposableHandle()
    }

    @AfterEach
    fun `run after each test`() {
        compositeDisposableHandle?.dispose()
        Dispatchers.resetMain()
        testDispatcher.cancel()
        testScope.cancel()
        runBlocking { shuttleWarehouse?.removeAllCargo() }
        File(CARGO_FILE_PATH).deleteRecursively()
    }

    @Test
    fun verifyBundleCargoWithBundleReturnsAShuttleBundle() = testScope.runTest {
        val bundleFactory = MockBundleFactory()
        val bundle = bundleFactory.create()
        val shuttleBundle = shuttle?.bundleCargoWith(bundle, bundleFactory)

        Assertions.assertNotNull(shuttleBundle)
    }

    @Test
    fun verifyIntentCargoWithIntentReturnsAShuttleIntent() = testScope.runTest {
        val intent = Intent()
        val shuttleIntent = shuttle?.intentCargoWith(intent)

        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentCargoWithActionReturnsAShuttleIntent() = testScope.runTest {
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val shuttleIntent = shuttle?.intentCargoWith(intent)

        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentCargoWithActionAndUriReturnsAShuttleIntent() = testScope.runTest {
        val uri = mock(Uri::class.java)
        val shuttleIntent = shuttle?.intentCargoWith(Intent.ACTION_MEDIA_BUTTON, uri)

        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentCargoWithContextAndClassReturnsAShuttleIntent() = testScope.runTest {
        val context = mock(Context::class.java)
        val shuttleIntent = shuttle?.intentCargoWith(context, TestActivity::class.java)

        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentCargoWithActionUriContextAndClassReturnsAShuttleIntent() = testScope.runTest {
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
    fun verifyIntentChooserCargoWithTargetAndTitleReturnsAShuttleIntent() = testScope.runTest {
        val title = "Test Title"
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val shuttleIntent = shuttle?.intentChooserCargoWith(intent, title)

        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyIntentChooserCargoWithTargetTitleAndSenderReturnsAShuttleIntent() = testScope.runTest {
        val title = "Test Title"
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val sender = mock(IntentSender::class.java)
        val shuttleIntent = shuttle?.intentChooserCargoWith(intent, title, sender)

        Assertions.assertNotNull(shuttleIntent)
    }

    @Test
    fun verifyPickupCargoSucceedsWithSuccessfulStore() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val filePath = "/cargo/$cargoId"
        var storedId = ""
        val countDownLatch = CountDownLatch(1)

        `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)
        `when`(dao.getCargoBy(cargoId)).thenReturn(CargoDataModel(cargoId, filePath))

        shuttle
            ?.intentCargoWith(Intent.ACTION_MEDIA_BUTTON)
            ?.transport(cargoId, cargo)
            ?.create()

        delay(1000L)

        launch(Dispatchers.Main) {
            val channel: Channel<ShuttlePickupCargoResult>? = shuttle?.pickupCargo<Cargo>(cargoId)
            channel
                ?.consumeAsFlow()
                ?.collectLatest { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttlePickupCargoResult.Loading -> {
                            /* ignore */
                        }
                        is ShuttlePickupCargoResult.Success<*> -> {
                            storedId = (shuttleResult.data as Cargo).cargoId
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttlePickupCargoResult.Error<*> -> {
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                    }
                }
        }.invokeOnCompletion {
            it?.let {
                println(it.message ?: "Error when getting the serializable.")
            }
        }.addForDisposal(compositeDisposableHandle)

        awaitOnLatch(countDownLatch, 1, TimeUnit.SECONDS)

        Assertions.assertEquals(cargoId, storedId)
    }

    @Test
    fun verifyCargoRemovalOnReturnToScreen() = testScope.runTest {
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val firstScreenClass = TestActivity::class.java
        val nextScreen = TestActivity2()
        val nextScreenClass = nextScreen::class.java
        val application = mock(Application::class.java)
        val warehouse = ShuttleDataWarehouse()
        val handler = mock(Handler::class.java)
        val facade = spy(ShuttleCargoFacade(application, warehouse, handler))
        val screenCallback = spy(facade.screenCallback)
        val cargoShuttle = CargoShuttle(facade, warehouse)
        val noCargo = "no cargo"
        var storeId = ""
        var channel: Channel<ShuttlePickupCargoResult>?
        val countDownLatch = CountDownLatch(3)

        runHandler(handler)
        cargoShuttle.cleanShuttleOnReturnTo(firstScreenClass, nextScreenClass, cargoId)
        awaitOnLatch(countDownLatch, 1, TimeUnit.SECONDS)

        cargoShuttle
            .intentCargoWith(Intent.ACTION_MEDIA_BUTTON)
            .transport(cargoId, cargo)
        screenCallback.onActivityCreated(nextScreen)
        nextScreen.onBackPressed()
        awaitOnLatch(countDownLatch, 1, TimeUnit.SECONDS)

        launch(Dispatchers.Main) {
            channel = cargoShuttle.pickupCargo<Cargo>(cargoId)
            channel
                ?.consumeAsFlow()
                ?.collectLatest { shuttleResult ->
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
        }.addForDisposal(compositeDisposableHandle)

        awaitOnLatch(countDownLatch, 1, TimeUnit.SECONDS)

        Assertions.assertEquals(noCargo, storeId)
    }

    // This suppression is okay.  This test requires more functionality.
    @Suppress("LongMethod")
    @Test
    fun verifyCargoRemovalOnCleanShuttleFromDeliveryFor() = testScope.runTest {
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val application = mock(Application::class.java)
        val warehouse = ShuttleDataWarehouse()
        val handler = mock(Handler::class.java)
        val facade = spy(ShuttleCargoFacade(application, warehouse, handler))
        val cargoShuttle = CargoShuttle(facade, warehouse)
        val noCargo = "no cargo"
        var storeId = ""
        var channel: Channel<ShuttlePickupCargoResult>?
        var numberOfValidSteps = 0
        val countDownLatch = CountDownLatch(2)

        cargoShuttle
            .intentCargoWith(Intent.ACTION_MEDIA_BUTTON)
            .transport(cargoId, cargo)
        delay(1000L)
        runHandler(handler)

        val removeCargoReceiverChannel = Channel<ShuttleRemoveCargoResult>()
        launch(Dispatchers.Main) {
            removeCargoReceiverChannel
                .consumeAsFlow()
                .collectLatest { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
                            countDownLatch.countDown()
                            cancel()
                        }
                        is ShuttleRemoveCargoResult.Removed -> {
                            numberOfValidSteps++
                            countDownLatch.countDown()
                            cancel()
                        }
                        is ShuttleRemoveCargoResult.Removing -> {
                            numberOfValidSteps++
                        }
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            countDownLatch.countDown()
                            cancel()
                        }
                    }
                }
        }.invokeOnCompletion {
            it?.let {
                println(it.message ?: "Error when getting the serializable.")
            }
        }.addForDisposal(compositeDisposableHandle)

        delay(1000L)

        cargoShuttle.cleanShuttleFromDeliveryFor(cargoId, removeCargoReceiverChannel)

        launch(Dispatchers.Main) {
            channel = cargoShuttle.pickupCargo<Cargo>(cargoId)
            channel
                ?.consumeAsFlow()
                ?.collectLatest { shuttleResult ->
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
        }.addForDisposal(compositeDisposableHandle)

        awaitOnLatch(countDownLatch, 1, TimeUnit.SECONDS)

        Assertions.assertEquals(noCargo, storeId)
        Assertions.assertEquals(2, numberOfValidSteps)
    }

    // This suppression is okay.  This test requires more functionality.
    @Suppress("LongMethod")
    @Test
    fun verifyCargoRemovalOnCleanShuttleFromAllDeliveries() = testScope.runTest {
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val application = mock(Application::class.java)
        val warehouse = ShuttleDataWarehouse()
        val handler = mock(Handler::class.java)
        val facade = spy(ShuttleCargoFacade(application, warehouse, handler))
        val cargoShuttle = CargoShuttle(facade, warehouse)
        val noCargo = "no cargo"
        var storeId = ""
        var channel: Channel<ShuttlePickupCargoResult>?
        var numberOfValidSteps = 0
        var countDownLatch = CountDownLatch(1)

        // Transport the cargo.  This adds the cargo to the filesystem.
        cargoShuttle
            .intentCargoWith(Intent.ACTION_MEDIA_BUTTON)
            .transport(cargoId, cargo)
        delay(1000L)
        runHandler(handler)

        // Remove the cargo
        val removeCargoReceiverChannel = Channel<ShuttleRemoveCargoResult>()
        launch(Dispatchers.Main) {
            removeCargoReceiverChannel
                .consumeAsFlow()
                .collectLatest { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
                            countDownLatch.countDown()
                            cancel()
                        }
                        is ShuttleRemoveCargoResult.Removed -> {
                            numberOfValidSteps++
                            countDownLatch.countDown()
                            cancel()
                        }
                        is ShuttleRemoveCargoResult.Removing -> {
                            numberOfValidSteps++
                        }
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            countDownLatch.countDown()
                            cancel()
                        }
                    }
                }
        }.invokeOnCompletion {
            it?.let {
                println(it.message ?: "Error when getting the serializable.")
            }
        }.addForDisposal(compositeDisposableHandle)

        awaitOnLatch(countDownLatch, 500, TimeUnit.MILLISECONDS)

        // Remove all of the cargo
        cargoShuttle.cleanShuttleFromAllDeliveries(removeCargoReceiverChannel)

        delay(1000L)
        countDownLatch = CountDownLatch(1)

        // Verify the lack of cargo by picking it up
        launch(Dispatchers.Main) {
            channel = cargoShuttle.pickupCargo<Cargo>(cargoId)
            channel
                ?.consumeAsFlow()
                ?.collectLatest { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttlePickupCargoResult.Loading -> {
                            /* ignore */
                        }
                        is ShuttlePickupCargoResult.Success<*> -> {
                            countDownLatch.countDown()
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
        }.addForDisposal(compositeDisposableHandle)

        awaitOnLatch(countDownLatch, 1, TimeUnit.SECONDS)

        Assertions.assertEquals(noCargo, storeId)
        Assertions.assertEquals(2, numberOfValidSteps)
    }

    private fun runHandler(handler: Handler) {
        doAnswer {
            val runnable = it.getArgument(0, Runnable::class.java)
            runnable?.run()
            true
        }.`when`(handler).post(Mockito.any())
    }

    @Suppress("SameParameterValue")
    private fun awaitOnLatch(countDownLatch: CountDownLatch, timeout: Long, timeUnit: TimeUnit) {
        @Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
        countDownLatch.await(timeout, timeUnit)
    }

    private data class CargoDataModel(override val cargoId: String, override val filePath: String) : ShuttleDataModel
    private data class Cargo(val cargoId: String, val numberOfBoxes: Int) : Serializable
    private class TestActivity : AppCompatActivity()
    private class TestActivity2 : AppCompatActivity()
}
