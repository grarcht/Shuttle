package com.grarcht.shuttle.framework.warehouse

import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.coroutines.CompositeDisposableHandle
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModel
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModelFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttleFileSystemGateway
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleStoreCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.nhaarman.mockitokotlin2.anyOrNull
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
class ShuttleWarehouseTests {
    private var compositeDisposableHandle: CompositeDisposableHandle? = null

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
    fun verifyStoreCargoSucceedsWithSuccessfulInsert() {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(1)
        var storedId = ""

        `when`(
            fileSystemGateway.writeToFile("$CARGO_FILE_PATH/cargo/", cargoId, cargo)
        ).thenReturn("$CARGO_FILE_PATH/cargo/$cargoId")
        `when`(dao.insertCargo(anyOrNull())).thenReturn(STORE_CARGO_SUCCESS)

        runBlocking {
            testScope = this

            // Will be launched in the mainThreadSurrogate dispatcher
            val disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttleStoreCargoResult> = warehouse.store(cargoId, cargo)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttleStoreCargoResult.Storing -> {
                                /* ignore */
                            }
                            is ShuttleStoreCargoResult.Success -> {
                                storedId = shuttleResult.cargoId
                                channel.cancel()
                                countDownLatch.countDown()
                            }
                            is ShuttleStoreCargoResult.Error<*> -> {
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
            compositeDisposableHandle?.add(disposableHandle)
        }

        countDownLatch.await(1, TimeUnit.SECONDS)
        Assertions.assertEquals(cargoId, storedId)
    }

    @Test
    fun verifyStoreCargoFailsWithNullCargo() {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val countDownLatch = CountDownLatch(1)

        runBlocking {
            testScope = this

            // Will be launched in the mainThreadSurrogate dispatcher
            val disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttleStoreCargoResult> = warehouse.store(cargoId, /* cargo */ null)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttleStoreCargoResult.Storing -> {
                                /* ignore */
                            }
                            is ShuttleStoreCargoResult.Success -> {
                                Assertions.fail<String>()
                                countDownLatch.countDown()
                            }
                            is ShuttleStoreCargoResult.Error<*> -> {
                                countDownLatch.countDown()
                                channel.cancel()
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
    }

    @Test
    fun verifyStoreCargoFailsWithANullFilePath() {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(1)

        `when`(
            fileSystemGateway.writeToFile("$CARGO_FILE_PATH/cargo/", cargoId, cargo)
        ).thenReturn(null)

        runBlocking {
            testScope = this

            // Will be launched in the mainThreadSurrogate dispatcher
            val disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttleStoreCargoResult> = warehouse.store(cargoId, cargo)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttleStoreCargoResult.Storing -> {
                                /* ignore */
                            }
                            is ShuttleStoreCargoResult.Success -> {
                                Assertions.fail<String>()
                                countDownLatch.countDown()
                            }
                            is ShuttleStoreCargoResult.Error<*> -> {
                                countDownLatch.countDown()
                                channel.cancel()
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
    }

    @Test
    fun verifyStoreCargoFailsWithFailedInsert() {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(1)

        `when`(
            fileSystemGateway.writeToFile("$CARGO_FILE_PATH/cargo/", cargoId, cargo)
        ).thenReturn("$CARGO_FILE_PATH/cargo/$cargoId")
        `when`(dao.insertCargo(anyOrNull())).thenReturn(STORE_CARGO_FAILURE)

        runBlocking {
            testScope = this

            // Will be launched in the mainThreadSurrogate dispatcher
            val disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttleStoreCargoResult> = warehouse.store(cargoId, cargo)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttleStoreCargoResult.Storing -> {
                                /* ignore */
                            }
                            is ShuttleStoreCargoResult.Success -> {
                                Assertions.fail<String>()
                                countDownLatch.countDown()
                            }
                            is ShuttleStoreCargoResult.Error<*> -> {
                                countDownLatch.countDown()
                                channel.cancel()
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
    }

    @Test
    fun verifyPickupCargoSucceedsWithSuccessfulInsert() {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(1)
        var storedId = ""
        val filePath = "$CARGO_FILE_PATH/cargo/$cargoId"

        `when`(
            fileSystemGateway.writeToFile("$CARGO_FILE_PATH/cargo/", cargoId, cargo)
        ).thenReturn(filePath)
        `when`(dao.insertCargo(anyOrNull())).thenReturn(STORE_CARGO_SUCCESS)


        runBlocking {
            testScope = this
            `when`(dao.getCargoBy(anyOrNull())).thenReturn(TestShuttleDataModel(cargoId, filePath))
            `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)

            // Will be launched in the mainThreadSurrogate dispatcher
            val disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttleStoreCargoResult> = warehouse.store(cargoId, cargo)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttleStoreCargoResult.Storing -> {
                                /* ignore */
                            }
                            is ShuttleStoreCargoResult.Success -> {
                                storedId = shuttleResult.cargoId
                                channel.cancel()
                                countDownLatch.countDown()
                            }
                            is ShuttleStoreCargoResult.Error<*> -> {
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
            compositeDisposableHandle?.add(disposableHandle)

            val disposableHandle2 = launch(Dispatchers.Main) {
                val channel: Channel<ShuttlePickupCargoResult> = warehouse.pickup<Cargo>(cargoId)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
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
            compositeDisposableHandle?.add(disposableHandle2)
        }

        countDownLatch.await(5, TimeUnit.SECONDS)
        Assertions.assertEquals(cargoId, storedId)
    }

    @Test
    fun verifyPickupCargoFailsWithNullCargoReturnedFromTheDAO() {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val countDownLatch = CountDownLatch(1)

        runBlocking {
            testScope = this
            `when`(dao.getCargoBy(anyOrNull())).thenReturn(/* ShuttleDataModel */ null)

            val disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttlePickupCargoResult> = warehouse.pickup<Cargo>(cargoId)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttlePickupCargoResult.Loading -> {
                                /* ignore */
                            }
                            is ShuttlePickupCargoResult.Success<*> -> {
                                countDownLatch.countDown()
                                Assertions.fail()

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
    }

    @Test
    fun verifyPickupCargoFailsWithNullCargoReturnedFromTheGateway() {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val countDownLatch = CountDownLatch(1)
        val filePath = "$CARGO_FILE_PATH/cargo/$cargoId"


        runBlocking {
            testScope = this
            `when`(dao.getCargoBy(anyOrNull())).thenReturn(TestShuttleDataModel(cargoId, filePath))
            `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(null)

            val disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttlePickupCargoResult> = warehouse.pickup<Cargo>(cargoId)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttlePickupCargoResult.Loading -> {
                                /* ignore */
                            }
                            is ShuttlePickupCargoResult.Success<*> -> {
                                countDownLatch.countDown()
                                Assertions.fail()

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
    }

    @Test
    fun verifyPickupCargoFailsWithCargoReturnedFromTheDAO() {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(1)
        var storedId = ""

        `when`(
            fileSystemGateway.writeToFile("$CARGO_FILE_PATH/cargo/", cargoId, cargo)
        ).thenReturn("$CARGO_FILE_PATH/cargo/$cargoId")
        `when`(dao.insertCargo(anyOrNull())).thenReturn(STORE_CARGO_SUCCESS)

        runBlocking {
            testScope = this

            // Will be launched in the mainThreadSurrogate dispatcher
            val disposableHandle = launch(Dispatchers.Main) {
                val channel: Channel<ShuttleStoreCargoResult> = warehouse.store(cargoId, cargo)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttleStoreCargoResult.Storing -> {
                                /* ignore */
                            }
                            is ShuttleStoreCargoResult.Success -> {
                                storedId = shuttleResult.cargoId
                                channel.cancel()
                                countDownLatch.countDown()
                            }
                            is ShuttleStoreCargoResult.Error<*> -> {
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
            compositeDisposableHandle?.add(disposableHandle)
        }

        countDownLatch.await(1, TimeUnit.SECONDS)
        Assertions.assertEquals(cargoId, storedId)
    }

    private data class Cargo(val cargoId: String, val numberOfBoxes: Int) : Serializable
    private data class TestShuttleDataModel(override val cargoId: String, override val filePath: String) : ShuttleDataModel
}