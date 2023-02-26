package com.grarcht.shuttle.framework.integrations.extensions.room

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions

private val context = InstrumentationRegistry.getInstrumentation().context
private val db = ShuttleRoomDataDb.getInstance(ShuttleRoomDbConfig(context))
private val dao = db.getShuttleDao()

/**
 * Verifies the functionality of the [ShuttleRoomDao].
 */
class ShuttleRoomDaoTests {
    @Test
    fun verifyDaoInsertCargo() {
        val shuttleRoomData = ShuttleRoomData()
        shuttleRoomData.cargoId = "cargoId1"
        shuttleRoomData.filePath = "/app/cargo/cargoId1"
        val insertResult = dao.insertCargo(shuttleRoomData)
        Assertions.assertTrue(insertResult >= 1L)
    }

    @Test
    fun verifyDaoGetNumberOfCargoItems() {
        val shuttleRoomData = ShuttleRoomData()
        shuttleRoomData.cargoId = "cargoId1"
        shuttleRoomData.filePath = "/app/cargo/cargoId1"
        dao.insertCargo(shuttleRoomData)

        val shuttleRoomData2 = ShuttleRoomData()
        shuttleRoomData2.cargoId = "cargoId2"
        shuttleRoomData2.filePath = "/app/cargo/cargoId2"
        dao.insertCargo(shuttleRoomData2)

        val numberOfCargoItems = dao.getNumberOfCargoItems()
        assertTrue(numberOfCargoItems == 2)
    }

    @Test
    fun verifyDaoGetCargoBy() {
        val shuttleRoomData = ShuttleRoomData()
        shuttleRoomData.cargoId = "cargoId1"
        shuttleRoomData.filePath = "/app/cargo/cargoId1"
        dao.insertCargo(shuttleRoomData)

        val shuttleRoomData2 = ShuttleRoomData()
        shuttleRoomData2.cargoId = "cargoId2"
        shuttleRoomData2.filePath = "/app/cargo/cargoId2"
        dao.insertCargo(shuttleRoomData2)

        runBlocking {
            val cargo1 = dao.getCargoById("cargoId1")
            Assertions.assertNotNull(cargo1)

            val cargo2 = dao.getCargoById("cargoId2")
            Assertions.assertNotNull(cargo2)
        }
    }


    @Test
    fun verifyDaoDeleteCargoBy() {
        val shuttleRoomData = ShuttleRoomData()
        shuttleRoomData.cargoId = "cargoId1"
        shuttleRoomData.filePath = "/app/cargo/cargoId1"
        dao.insertCargo(shuttleRoomData)
        val deleteResult = dao.deleteCargoBy(cargoId = shuttleRoomData.cargoId)
        Assertions.assertTrue(deleteResult == 1)
    }

    @Test
    fun verifyDaoDeleteAllCargo() {
        val shuttleRoomData1 = ShuttleRoomData()
        shuttleRoomData1.cargoId = "cargoId1"
        shuttleRoomData1.filePath = "/app/cargo/cargoId1"
        dao.insertCargo(shuttleRoomData1)

        val shuttleRoomData2 = ShuttleRoomData()
        shuttleRoomData2.cargoId = "cargoId2"
        shuttleRoomData2.filePath = "/app/cargo/cargoId2"
        dao.insertCargo(shuttleRoomData2)

        val shuttleRoomData3 = ShuttleRoomData()
        shuttleRoomData3.cargoId = "cargoId3"
        shuttleRoomData3.filePath = "/app/cargo/cargoId3"
        dao.insertCargo(shuttleRoomData3)

        val deleteResult = dao.deleteAllCargoData()
        val numberOfCargoItemsAfterDeletion = dao.getNumberOfCargoItems()

        Assertions.assertTrue(deleteResult == 3)
        Assertions.assertTrue(numberOfCargoItemsAfterDeletion == 0)
    }

    // To use the before and after functions, they need to be in the companion object.
    companion object {
        @Before
        fun cleanUpDbBeforeAllTests() {
            dao.deleteAllCargoData()
        }

        @After
        fun cleanUpDbAfterEachTest() {
            dao.deleteAllCargoData()
        }

        @JvmStatic
        @AfterClass
        fun cleanUpDbAfterAllTests() {
            db.close()
        }
    }
}