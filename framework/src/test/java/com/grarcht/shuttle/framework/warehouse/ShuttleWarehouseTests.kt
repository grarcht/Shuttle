package com.grarcht.shuttle.framework.warehouse

import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.coroutines.CompositeDisposableHandle
import com.grarcht.shuttle.framework.coroutines.addForDisposal
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModel
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModelFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttleFileSystemGateway
import com.grarcht.shuttle.framework.integrations.persistence.result.ShuttlePersistenceRemoveCargoResult
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.result.ShuttleStoreCargoResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import java.io.File
import java.io.Serializable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val CARGO_FILE_PATH = "/cargo"
private const val STORE_CARGO_FAILURE = -1L
private const val STORE_CARGO_SUCCESS = 1L

@ExperimentalCoroutinesApi
@Suppress("LargeClass") // It's okay for this class.  There are just different test cases.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShuttleWarehouseTests {
    private var compositeDisposableHandle: CompositeDisposableHandle? = null
    private var shuttle: Shuttle? = null
    private var shuttleWarehouse: ShuttleDataWarehouse? = null
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: TestScope

    @BeforeEach
    fun `run before each test`() {
        testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
        testScope = TestScope()
        Dispatchers.setMain(testDispatcher)
        shuttleWarehouse = ShuttleDataWarehouse()
        shuttle = Mockito.spy(CargoShuttle(mock(), shuttleWarehouse as ShuttleDataWarehouse))
        compositeDisposableHandle = CompositeDisposableHandle()
    }

    @AfterEach
    fun `run after each test`() {
        compositeDisposableHandle?.dispose()
        Dispatchers.resetMain()
        testDispatcher.cancel()
        testScope.cancel()
    }

    @AfterAll
    fun `run after all tests`() {
        runBlocking { shuttleWarehouse?.removeAllCargo() }
        File(CARGO_FILE_PATH).deleteRecursively()
        compositeDisposableHandle?.dispose()
        Dispatchers.resetMain()
        testDispatcher.cancel()
        testScope.cancel()
    }

    @Test
    fun verifyStoreCargoSucceedsWithSuccessfulInsert() = testScope.runTest {
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

        delay(1000L)
        Assertions.assertEquals(cargoId, storedId)
    }

    @Test
    fun verifyStoreCargoFailsWithNullCargo() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val countDownLatch = CountDownLatch(1)

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

        delay(1000L)
    }

    @Test
    fun verifyStoreCargoFailsWithANullFilePath() = testScope.runTest {
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

        delay(1000L)
    }

    @Test
    fun verifyStoreCargoFailsWithFailedInsert() = testScope.runTest {
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


        delay(1000L)
    }

    @Test
    fun verifyPickupCargoSucceedsWithSuccessfulInsert() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(2)
        var storedId = ""
        val filePath = "$CARGO_FILE_PATH/cargo/$cargoId"

        `when`(
            fileSystemGateway.writeToFile("$CARGO_FILE_PATH/cargo/", cargoId, cargo)
        ).thenReturn(filePath)
        `when`(dao.insertCargo(anyOrNull())).thenReturn(STORE_CARGO_SUCCESS)
        `when`(dao.getCargoBy(anyOrNull())).thenReturn(TestShuttleDataModel(cargoId, filePath))
        `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)

        storeCargo(cargo, warehouse)
        delay(1000L)

        // Will be launched in the mainThreadSurrogate dispatcher
        val disposableHandle = launch(Dispatchers.Main) {
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
        compositeDisposableHandle?.add(disposableHandle)

        delay(1000L)
        Assertions.assertEquals(cargoId, storedId)
    }

    @Test
    fun verifyPickupCargoFailsWithNullCargoReturnedFromTheDAO() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val countDownLatch = CountDownLatch(1)

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

        delay(1000L)
    }

    @Test
    fun verifyPickupCargoFailsWithNullCargoReturnedFromTheGateway() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val countDownLatch = CountDownLatch(1)
        val filePath = "$CARGO_FILE_PATH/cargo/$cargoId"

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

        delay(1000L)
    }

    @Test
    fun verifyPickupCargoFailsWithCargoReturnedFromTheDAO() = testScope.runTest {
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

        delay(1000L)
        Assertions.assertEquals(cargoId, storedId)
    }

    @Test
    fun verifyRemovingCargoByIdSucceedsWithAValidId() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(2)
        var removedCargoId = ""
        val filePath = "$CARGO_FILE_PATH/cargo/$cargoId"
        var successfulStepsMet = 0

        `when`(
            fileSystemGateway.writeToFile("$CARGO_FILE_PATH/cargo/", cargoId, cargo)
        ).thenReturn(filePath)
        `when`(dao.insertCargo(anyOrNull())).thenReturn(STORE_CARGO_SUCCESS)
        `when`(fileSystemGateway.deleteFile(filePath)).thenReturn(ShuttlePersistenceRemoveCargoResult.Removed)
        `when`(dao.getCargoBy(anyOrNull())).thenReturn(TestShuttleDataModel(cargoId, filePath))
        `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)

        storeCargo(cargo, warehouse)
        delay(1000L)

        // Will be launched in the mainThreadSurrogate dispatcher
        val disposableHandle = launch(Dispatchers.Main) {
            val channel: Channel<ShuttleRemoveCargoResult> = warehouse.removeCargoBy(cargoId)
            channel.consumeAsFlow()
                .collect { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttleRemoveCargoResult.Removing -> {
                            successfulStepsMet++
                        }
                        is ShuttleRemoveCargoResult.Removed -> {
                            successfulStepsMet++
                            removedCargoId = shuttleResult.cargoId
                            channel.cancel()
                            countDownLatch.countDown()
                        }
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            channel.cancel()
                            countDownLatch.countDown()
                        }
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
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

        delay(1000L)
        Assertions.assertEquals(2, successfulStepsMet)
        Assertions.assertEquals(cargoId, removedCargoId)
    }

    @Test
    fun verifyRemovingCargoByIdFailsWithAnInvalidId() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val invalidCargoId = "cargoId25"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(2)
        val removedCargoId = ""
        val filePath = "$CARGO_FILE_PATH/cargo/$cargoId"
        val invalidFilePath = "$CARGO_FILE_PATH/cargo/$invalidCargoId"
        var failureStepsMet = 0

        `when`(
            fileSystemGateway.writeToFile("$CARGO_FILE_PATH/cargo/", cargoId, cargo)
        ).thenReturn(filePath)
        `when`(fileSystemGateway.deleteFile(invalidFilePath))
            .thenReturn(ShuttlePersistenceRemoveCargoResult.DoesNotExist)
        `when`(dao.getCargoBy(anyOrNull()))
            .thenReturn(TestShuttleDataModel(cargoId, filePath))
        `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)

        storeCargo(cargo, warehouse)
        delay(1000L)

        // Will be launched in the mainThreadSurrogate dispatcher
        val disposableHandle = launch(Dispatchers.Main) {
            val channel: Channel<ShuttleRemoveCargoResult> = warehouse.removeCargoBy(invalidCargoId)
            channel.consumeAsFlow()
                .collect { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttleRemoveCargoResult.Removing -> {
                            failureStepsMet++
                        }
                        is ShuttleRemoveCargoResult.Removed -> {
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
                            failureStepsMet++
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

        delay(1000L)
        Assertions.assertEquals(2, failureStepsMet)
        Assertions.assertEquals("", removedCargoId)
    }

    @Test
    fun verifyRemovingCargoByIdFailsWhenFileCannotBeDeleted() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val invalidCargoId = "cargoId25"
        val cargo = Cargo(cargoId, 10)
        val removedCargoId = ""
        val filePath = "$CARGO_FILE_PATH/cargo/$cargoId"
        val invalidFilePath = "$CARGO_FILE_PATH/cargo/$invalidCargoId"
        var failureStepsMet = 0

        `when`(
            fileSystemGateway.writeToFile("$CARGO_FILE_PATH/cargo/", cargoId, cargo)
        ).thenReturn(filePath)
        `when`(dao.getCargoBy(anyOrNull())).thenReturn(TestShuttleDataModel(cargoId, filePath))
        `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)
        `when`(
            fileSystemGateway.deleteFile(invalidFilePath)
        ).thenReturn(ShuttlePersistenceRemoveCargoResult.UnableToRemove)

        storeCargo(cargo, warehouse)
        delay(1000L)

        launch(Dispatchers.Main) {
            val channel: Channel<ShuttleRemoveCargoResult> = warehouse.removeCargoBy(invalidCargoId)
            channel.consumeAsFlow()
                .collect { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttleRemoveCargoResult.Removing -> {
                            failureStepsMet++
                        }
                        is ShuttleRemoveCargoResult.Removed -> {
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            failureStepsMet++
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
                            channel.cancel()
                        }
                    }
                }
        }.invokeOnCompletion {
            it?.let {
                println(it.message ?: "Error when getting the serializable.")
            }
        }.addForDisposal(compositeDisposableHandle)

        delay(1000L)
        Assertions.assertEquals(2, failureStepsMet)
        Assertions.assertEquals("", removedCargoId)
    }

    @Test
    fun verifyRemovingCargoByIdFailsWhenDAORemovalFails() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(2)
        val removedCargoId = ""
        val filePath = "$CARGO_FILE_PATH/cargo/$cargoId"
        var failureStepsMet = 0

        `when`(
            fileSystemGateway.writeToFile("$CARGO_FILE_PATH/cargo/", cargoId, cargo)
        ).thenReturn(filePath)
        `when`(dao.getCargoBy(anyOrNull())).thenReturn(TestShuttleDataModel(cargoId, filePath))
        `when`(dao.deleteCargoBy(cargoId)).thenReturn(ShuttleDataAccessObject.REMOVE_CARGO_FAILED)
        `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)
        `when`(
            fileSystemGateway.deleteFile(filePath)
        ).thenReturn(ShuttlePersistenceRemoveCargoResult.UnableToRemove)

        storeCargo(cargo, warehouse)
        delay(1000L)

        // Will be launched in the mainThreadSurrogate dispatcher
        launch(Dispatchers.Main) {
            val channel: Channel<ShuttleRemoveCargoResult> = warehouse.removeCargoBy(cargoId)
            channel.consumeAsFlow()
                .collect { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttleRemoveCargoResult.Removing -> {
                            failureStepsMet++
                        }
                        is ShuttleRemoveCargoResult.Removed -> {
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            failureStepsMet++
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
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

        awaitOnLatch(countDownLatch, 1L, TimeUnit.SECONDS)
        Assertions.assertEquals(2, failureStepsMet)
        Assertions.assertEquals("", removedCargoId)
    }

    @Test
    fun verifyRemovingAllCargoSucceedsWhenDAORemovalSucceeds() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(2)
        var removedCargoId = ""
        val directory = "$CARGO_FILE_PATH/cargo"
        val filePath = "$directory/$cargoId"
        var successfulStepsMet = 0

        `when`(
            fileSystemGateway.writeToFile(CARGO_FILE_PATH, cargoId, cargo)
        ).thenReturn(filePath)
        `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)
        `when`(dao.deleteAllCargoData()).thenReturn(1)
        `when`(
            fileSystemGateway.deleteAllFilesAt(anyOrNull())
        ).thenReturn(ShuttlePersistenceRemoveCargoResult.Removed)

        storeCargo(cargo, warehouse)
        delay(1000L)

        launch(Dispatchers.Main) {
            val channel: Channel<ShuttleRemoveCargoResult> = warehouse.removeAllCargo()
            channel.consumeAsFlow()
                .collect { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttleRemoveCargoResult.Removing -> {
                            successfulStepsMet++
                        }
                        is ShuttleRemoveCargoResult.Removed -> {
                            successfulStepsMet++
                            removedCargoId = shuttleResult.cargoId
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
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

        awaitOnLatch(countDownLatch, 3L, TimeUnit.SECONDS)
        Assertions.assertEquals(2, successfulStepsMet)
        Assertions.assertEquals(ShuttleRemoveCargoResult.ALL_CARGO, removedCargoId)
    }

    @Test
    fun verifyRemovingAllCargoFailsWhenGatewayRemovalFailsToRemoveIt() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(2)
        var removedCargoId = ""
        val directory = "$CARGO_FILE_PATH/cargo"
        val filePath = "$directory/$cargoId"
        var successfulStepsMet = 0

        `when`(
            fileSystemGateway.writeToFile(CARGO_FILE_PATH, cargoId, cargo)
        ).thenReturn(filePath)
        `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)
        `when`(dao.deleteAllCargoData()).thenReturn(1)
        `when`(
            fileSystemGateway.deleteAllFilesAt(anyOrNull())
        ).thenReturn(ShuttlePersistenceRemoveCargoResult.UnableToRemove)

        storeCargo(cargo, warehouse)
        delay(1000L)

        launch(Dispatchers.Main) {
            val channel: Channel<ShuttleRemoveCargoResult> = warehouse.removeAllCargo()
            channel.consumeAsFlow()
                .collect { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttleRemoveCargoResult.Removing -> {
                            successfulStepsMet++
                        }
                        is ShuttleRemoveCargoResult.Removed -> {
                            removedCargoId = shuttleResult.cargoId
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            successfulStepsMet++
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
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

        awaitOnLatch(countDownLatch, 3L, TimeUnit.SECONDS)
        Assertions.assertEquals(2, successfulStepsMet)
        Assertions.assertEquals("", removedCargoId)
    }

    @Test
    fun verifyRemovingAllCargoFailsWhenTheDirectoryDoesNotExist() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(2)
        var removedCargoId = ""
        val directory = "$CARGO_FILE_PATH/cargo"
        val filePath = "$directory/$cargoId"
        var successfulStepsMet = 0

        `when`(
            fileSystemGateway.writeToFile(CARGO_FILE_PATH, cargoId, cargo)
        ).thenReturn(filePath)
        `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)
        `when`(dao.deleteAllCargoData()).thenReturn(1)
        `when`(
            fileSystemGateway.deleteAllFilesAt(anyOrNull())
        ).thenReturn(ShuttlePersistenceRemoveCargoResult.DoesNotExist)

        storeCargo(cargo, warehouse)
        delay(1000L)

        launch(Dispatchers.Main) {
            val channel: Channel<ShuttleRemoveCargoResult> = warehouse.removeAllCargo()
            channel.consumeAsFlow()
                .collect { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttleRemoveCargoResult.Removing -> {
                            successfulStepsMet++
                        }
                        is ShuttleRemoveCargoResult.Removed -> {
                            removedCargoId = shuttleResult.cargoId
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
                            successfulStepsMet++
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

        awaitOnLatch(countDownLatch, 3L, TimeUnit.SECONDS)
        Assertions.assertEquals(2, successfulStepsMet)
        Assertions.assertEquals("", removedCargoId)
    }

    @Test
    fun verifyRemovingAllCargoFailsWhenDAOFailsToRemoveIt() = testScope.runTest {
        val dao = mock(ShuttleDataAccessObject::class.java)
        val dataModelFactory = mock(ShuttleDataModelFactory::class.java)
        val fileSystemGateway = mock(ShuttleFileSystemGateway::class.java)
        val warehouse: ShuttleWarehouse = ShuttleRepository(dao, dataModelFactory, CARGO_FILE_PATH, fileSystemGateway)
        val cargoId = "cargoId1"
        val cargo = Cargo(cargoId, 10)
        val countDownLatch = CountDownLatch(2)
        var removedCargoId = ""
        val directory = "$CARGO_FILE_PATH/cargo"
        val filePath = "$directory/$cargoId"
        var failureStepsMet = 0

        `when`(
            fileSystemGateway.writeToFile(CARGO_FILE_PATH, cargoId, cargo)
        ).thenReturn(filePath)
        `when`(fileSystemGateway.readFromFile(filePath)).thenReturn(cargo)
        `when`(dao.deleteAllCargoData()).thenReturn(ShuttleDataAccessObject.REMOVE_CARGO_FAILED)
        `when`(
            fileSystemGateway.deleteAllFilesAt(anyOrNull())
        ).thenReturn(ShuttlePersistenceRemoveCargoResult.Removed)

        storeCargo(cargo, warehouse)
        delay(1000L)

        launch(Dispatchers.Main) {
            val channel: Channel<ShuttleRemoveCargoResult> = warehouse.removeAllCargo()
            channel.consumeAsFlow()
                .collect { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttleRemoveCargoResult.Removing -> {
                            failureStepsMet++
                        }
                        is ShuttleRemoveCargoResult.Removed -> {
                            removedCargoId = shuttleResult.cargoId
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                            failureStepsMet++
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
                            countDownLatch.countDown()
                            channel.cancel()
                        }
                    }
                }
        }.invokeOnCompletion {
            it?.let {
                println(it.message ?: "Error when removing all of the cargo.")
            }
        }.addForDisposal(compositeDisposableHandle)

        awaitOnLatch(countDownLatch, 3L, TimeUnit.SECONDS)
        Assertions.assertEquals(2, failureStepsMet)
        Assertions.assertEquals("", removedCargoId)
    }

    private fun storeCargo(
        cargo: Cargo,
        warehouse: ShuttleWarehouse
    ) {
        runBlocking {
            launch(Dispatchers.Main) {
                val channel: Channel<ShuttleStoreCargoResult> = warehouse.store(cargo.cargoId, cargo)
                channel.consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            is ShuttleStoreCargoResult.Storing -> {
                                /* ignore */
                            }
                            is ShuttleStoreCargoResult.Success -> {
                                channel.cancel()
                            }
                            is ShuttleStoreCargoResult.Error<*> -> {
                                channel.cancel()
                            }
                        }
                    }
            }.invokeOnCompletion {
                it?.let {
                    println(it.message ?: "Error when getting the serializable.")
                }
            }.addForDisposal(compositeDisposableHandle)
        }
    }

    @Suppress("SameParameterValue")
    private fun awaitOnLatch(countDownLatch: CountDownLatch, timeout: Long, timeUnit: TimeUnit) {
        @Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
        countDownLatch.await(timeout, timeUnit)
    }

    private data class Cargo(val cargoId: String, val numberOfBoxes: Int) : Serializable
    private data class TestShuttleDataModel(
        override val cargoId: String,
        override val filePath: String
    ) : ShuttleDataModel
}
