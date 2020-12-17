package com.grarcht.shuttle.framework.bundle

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.content.ShuttleDataExtractor
import com.grarcht.shuttle.framework.content.ShuttleResult
import com.grarcht.shuttle.framework.content.bundle.ShuttleBundle
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttleBundleTest {
    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    private val intent = mock(Intent::class.java)

    @OptIn(ObsoleteCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private val shuttleWarehouse = TestRepository()
    private var testScope: CoroutineScope? = null
    private var disposableHandle: DisposableHandle? = null
    private lateinit var shuttleDataExtractor: ShuttleDataExtractor

    @Volatile
    private var doesBundleMatch = false

    @Volatile
    private var resultBundle: Bundle? = null

    @BeforeAll
    fun runBeforeAllTests() {
        //https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
        Dispatchers.setMain(mainThreadSurrogate)
        //val intent: Intent = mock(Intent::class.java)
        shuttleDataExtractor = spy(ShuttleDataExtractor(shuttleWarehouse))
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
        val lifecycleOwner = mock(LifecycleOwner::class.java)
        var countDownLatch = CountDownLatch(1)
        val nestedBundleKey = "nestedBundle"
        val paintColorKey = "paint color"
        val map = mutableMapOf<String?, Any?>(Pair(paintColorKey, "blue"))
        val bundleToCreateFrom = MockBundleFactory().create(map)
        val shuttleBundle = ShuttleBundle.with(bundleToCreateFrom, shuttleWarehouse)

        // when
        shuttleBundle.safelyPutBundle(nestedBundleKey, bundleToCreateFrom)
        val bundle = shuttleBundle.create()
        countDownLatch.await(1, TimeUnit.SECONDS)
        countDownLatch = CountDownLatch(1)


        // verify
        runBlocking {
            testScope = this

            // Will be launched in the mainThreadSurrogate dispatcher
            disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttleResult> = shuttleDataExtractor.extractParcelData(
                    bundle = bundle,
                    key = nestedBundleKey,
                    parcelableCreator = MockBundleFactory.creator
                ) { TestLifecycle() }

                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            ShuttleResult.Loading -> {
                                /* ignore */
                            }
                            is ShuttleResult.Success<*> -> {
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
                            is ShuttleResult.Error<*> -> {
                                countDownLatch.countDown()
                                fail()
                            }
                        }
                    }
            }.invokeOnCompletion {
                it?.let {
                    println(it?.message ?: "Error when getting bundle.")
                }
            }
        }

        assertEquals(2, shuttleWarehouse.numberOfSaveInvocations)
        countDownLatch.await(1, TimeUnit.SECONDS)
        assertTrue(doesBundleMatch)
    }

    class TestRepository : ShuttleDataWarehouse() {
        private val getChannel = Channel<ShuttleResult>(5)

        override suspend fun <D : Parcelable> get(
            lookupKey: String,
            parcelableCreator: Parcelable.Creator<D>,
            lifecycleOwner: LifecycleOwner
        ): Channel<ShuttleResult> {
            val parcelable = parcelableToEmit
            println("parcelableToEmit $parcelable")
            try {
                getChannel.send(ShuttleResult.Success(parcelable))
            } catch (e: Exception) {
                println("caught: $e")
            }
            return getChannel
        }

        override suspend fun <D : Parcelable> save(lookupKey: String, data: D?) {
            super.save(lookupKey, data)
            parcelableToEmit = data as Parcelable
        }
    }

    private class TestLifecycle : Lifecycle() {
        override fun addObserver(observer: LifecycleObserver) {
            // Ignore
        }

        override fun removeObserver(observer: LifecycleObserver) {
            // Ignore
        }

        override fun getCurrentState(): State {
            return State.STARTED
        }
    }
}