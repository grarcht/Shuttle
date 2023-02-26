package com.grarcht.shuttle.framework.intent

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import androidx.fragment.app.Fragment
import com.grarcht.shuttle.framework.Cargo
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.content.ShuttleIntent
import com.grarcht.shuttle.framework.coroutines.CompositeDisposableHandle
import com.grarcht.shuttle.framework.coroutines.addForDisposal
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleDataWarehouse
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
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
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import java.io.Serializable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttleIntentTests {
    private var compositeDisposableHandle: CompositeDisposableHandle? = null
    private var disposableHandle: DisposableHandle? = null

    @Volatile
    private var doesResultMatch = false

    @Volatile
    private var resultSerializable: Serializable? = null

    private var shuttle: Shuttle? = null
    private var shuttleWarehouse: ShuttleDataWarehouse? = null
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: TestScope

    @BeforeEach
    fun `run before each test`() {
        testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
        testScope = TestScope()
        Dispatchers.setMain(testDispatcher)
        compositeDisposableHandle = CompositeDisposableHandle()
        val shuttleScreenFacade = mock(ShuttleFacade::class.java)
        shuttleWarehouse = ShuttleDataWarehouse()
        shuttle = Mockito.spy(CargoShuttle(shuttleScreenFacade, shuttleWarehouse as ShuttleWarehouse))
        doesResultMatch = false
    }

    @AfterEach
    fun `run after each test`() {
        runBlocking { shuttleWarehouse?.removeAllCargo() }
        disposableHandle?.dispose()
        compositeDisposableHandle?.dispose()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        testDispatcher.cancel()
        testScope.cancel()
    }

    @Test
    fun verifyCreatingShuttleIntentWithAnotherIntent() = testScope.runTest {
        val warehouse: ShuttleWarehouse = mock(ShuttleWarehouse::class.java)
        val firstIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val shuttleIntent = ShuttleIntent
            .with(warehouse)
            .intent(firstIntent)
        val intent = shuttleIntent.create()

        Assertions.assertNotNull(shuttleIntent)
        Assertions.assertNotNull(intent)
    }

    @Test
    fun verifyCreatingShuttleIntentWithAnAction() = testScope.runTest {
        val warehouse: ShuttleWarehouse = mock(ShuttleWarehouse::class.java)
        val shuttleIntent = ShuttleIntent
            .with(warehouse)
            .intent(Intent.ACTION_MEDIA_BUTTON)
        val intent = shuttleIntent.create()

        Assertions.assertNotNull(shuttleIntent)
        Assertions.assertNotNull(intent)
    }

    @Test
    fun verifyCreatingShuttleIntentWithAnActionAndUri() = testScope.runTest {
        val warehouse: ShuttleWarehouse = mock(ShuttleWarehouse::class.java)
        val uri = mock(Uri::class.java)
        val shuttleIntent = ShuttleIntent
            .with(warehouse)
            .intent(Intent.ACTION_MEDIA_BUTTON, uri)
        val intent = shuttleIntent.create()

        Assertions.assertNotNull(shuttleIntent)
        Assertions.assertNotNull(intent)
    }

    @Test
    fun verifyCreatingShuttleIntentWithContextAndClass() = testScope.runTest {
        val warehouse: ShuttleWarehouse = mock(ShuttleWarehouse::class.java)
        val context: Context = mock(Context::class.java)
        val shuttleIntent = ShuttleIntent
            .with(warehouse)
            .intent(context, Fragment::class.java)
        val intent = shuttleIntent.create()

        Assertions.assertNotNull(shuttleIntent)
        Assertions.assertNotNull(intent)
    }


    @Test
    fun verifyCreatingShuttleIntentWithAnActionUriContextAndClass() = testScope.runTest {
        val warehouse: ShuttleWarehouse = mock(ShuttleWarehouse::class.java)
        val context: Context = mock(Context::class.java)
        val uri = mock(Uri::class.java)
        val shuttleIntent = ShuttleIntent
            .with(warehouse)
            .intent(Intent.ACTION_MEDIA_BUTTON, uri, context, Fragment::class.java)
        val intent = shuttleIntent.create()

        Assertions.assertNotNull(shuttleIntent)
        Assertions.assertNotNull(intent)
    }

    @Test
    fun verifyCreatingShuttleIntentChooserWithIntentAndTitle() = testScope.runTest {
        val warehouse: ShuttleWarehouse = mock(ShuttleWarehouse::class.java)
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val title = "Video Player"
        val intentFactory: MockedStatic<Intent> = mockStatic(Intent::class.java)
        intentFactory
            .`when`<Intent> { Intent.createChooser(intent, title) }
            .thenReturn(Intent(Intent.ACTION_MEDIA_BUTTON))
        val shuttleIntent = ShuttleIntent
            .with(warehouse)
            .intentChooser(intent, title)
        val resultIntent = shuttleIntent.create()

        Assertions.assertNotNull(shuttleIntent)
        Assertions.assertNotNull(resultIntent)

        intentFactory.close()
    }

    @Test
    fun verifyCreatingShuttleIntentChooserWithTwoIntentsAndTitle() = testScope.runTest {
        val warehouse: ShuttleWarehouse = mock(ShuttleWarehouse::class.java)
        val targetIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val senderIntent = mock(IntentSender::class.java)
        val title = "Video Player"
        val intentFactory: MockedStatic<Intent> = mockStatic(Intent::class.java)
        intentFactory
            .`when`<Intent> { Intent.createChooser(targetIntent, title, senderIntent) }
            .thenReturn(Intent(Intent.ACTION_MEDIA_BUTTON))
        val shuttleIntent = ShuttleIntent
            .with(warehouse)
            .intentChooser(targetIntent, title, senderIntent)
        val intent = shuttleIntent.create()

        Assertions.assertNotNull(shuttleIntent)
        Assertions.assertNotNull(intent)

        intentFactory.close()
    }

    @Test
    fun verifyTransportOfSerializableData() = testScope.runTest {
        val firstIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val cargoId = "cargoId1"
        val numOfBoxes = 50
        val countDownLatch = CountDownLatch(1)

        ShuttleIntent
            .with(shuttleWarehouse as ShuttleWarehouse)
            .intent(firstIntent)
            .transport(cargoId, Cargo(cargoId, numOfBoxes))

        delay(1000L)

        launch(Dispatchers.Main) {

            val defaultResult = Channel<ShuttlePickupCargoResult>(1)
            val channel: Channel<ShuttlePickupCargoResult> = shuttle?.pickupCargo<Cargo>(cargoId) ?: defaultResult
            channel.consumeAsFlow()
                .collectLatest { shuttleResult ->
                    when (shuttleResult) {
                        ShuttlePickupCargoResult.Loading -> {
                            /* ignore */
                        }
                        is ShuttlePickupCargoResult.Success<*> -> {
                            resultSerializable = shuttleResult.data as Serializable
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttlePickupCargoResult.Error<*> -> {
                            Assertions.fail()
                        }
                    }
                }
        }.invokeOnCompletion {
            it?.let {
                println(it.message ?: "Error when getting the serializable.")
            }
        }.addForDisposal(compositeDisposableHandle)

        awaitOnLatch(countDownLatch, 1L, TimeUnit.SECONDS)


        // Verify
        Assertions.assertEquals(1, shuttleWarehouse?.numberOfStoreInvocations)
        val cargo = resultSerializable as Cargo
        Assertions.assertEquals(numOfBoxes, cargo.numberOfBoxes)
    }

    @Suppress("SameParameterValue")
    private fun awaitOnLatch(countDownLatch: CountDownLatch, timeout: Long, timeUnit: TimeUnit) {
        @Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
        countDownLatch.await(timeout, timeUnit)
    }
}
