package com.grarcht.shuttle.framework.intent

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import androidx.fragment.app.Fragment
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.bundle.ShuttleDataWarehouse
import com.grarcht.shuttle.framework.content.ShuttleIntent
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import java.io.Serializable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttleIntentTests {
    private var disposableHandle: DisposableHandle? = null

    @Volatile
    private var doesResultMatch = false

    @OptIn(ObsoleteCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Volatile
    private var resultSerializable: Serializable? = null

    private var shuttle: Shuttle? = null
    private val shuttleWarehouse = ShuttleDataWarehouse()
    private var testScope: CoroutineScope? = null

    @ExperimentalCoroutinesApi // This is only for the call to Dispatchers.setMain
    @BeforeAll
    fun runBeforeAllTests() {
        //https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
        Dispatchers.setMain(mainThreadSurrogate)

        val shuttleScreenFacade = Mockito.mock(ShuttleFacade::class.java)

        shuttle = Mockito.spy(CargoShuttle(shuttleScreenFacade, shuttleWarehouse))
        doesResultMatch = false
    }

    @ExperimentalCoroutinesApi // This is only for the call to Dispatchers.resetMain
    @AfterAll
    fun tearDown() {
        runBlocking {
            shuttleWarehouse.removeAllCargo()
        }
        disposableHandle?.dispose()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
        testScope?.cancel()
    }

    @Test
    fun verifyCreatingShuttleIntentWithAnotherIntent() {
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
    fun verifyCreatingShuttleIntentWithAnAction() {
        val warehouse: ShuttleWarehouse = mock(ShuttleWarehouse::class.java)
        val shuttleIntent = ShuttleIntent
            .with(warehouse)
            .intent(Intent.ACTION_MEDIA_BUTTON)
        val intent = shuttleIntent.create()
        Assertions.assertNotNull(shuttleIntent)
        Assertions.assertNotNull(intent)
    }

    @Test
    fun verifyCreatingShuttleIntentWithAnActionAndUri() {
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
    fun verifyCreatingShuttleIntentWithContextAndClass() {
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
    fun verifyCreatingShuttleIntentWithAnActionUriContextAndClass() {
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
    fun verifyCreatingShuttleIntentChooserWithIntentAndTitle() {
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
    fun verifyCreatingShuttleIntentChooserWithTwoIntentsAndTitle() {
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
    fun verifyTransportOfSerializableData() {
        var countDownLatch = CountDownLatch(1)
        val firstIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val cargoId = "cargoId1"
        val numOfBoxes = 50
        ShuttleIntent
            .with(shuttleWarehouse)
            .intent(firstIntent)
            .transport(cargoId, Cargo(numOfBoxes))

        countDownLatch.await(1, TimeUnit.SECONDS)
        countDownLatch = CountDownLatch(1)

        // verify
        runBlocking {
            testScope = this

            // Will be launched in the mainThreadSurrogate dispatcher
            disposableHandle = launch(Dispatchers.Main) {

                val defaultResult = Channel<ShuttlePickupCargoResult>(1)
                val channel: Channel<ShuttlePickupCargoResult> = shuttle?.pickupCargo<Cargo>(cargoId) ?: defaultResult
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
                                Assertions.fail()
                            }
                        }
                    }
            }.invokeOnCompletion {
                it?.let {
                    println(it.message ?: "Error when getting the serializable.")
                }
            }
        }

        countDownLatch.await(1, TimeUnit.SECONDS)
        Assertions.assertEquals(1, shuttleWarehouse.numberOfStoreInvocations)
        val cargo = resultSerializable as Cargo
        Assertions.assertEquals(numOfBoxes, cargo.boxes)
    }

    private data class Cargo(val boxes: Int) : Serializable
}