package com.grarcht.shuttle.framework.integrations.extensions.room

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

private const val CARGO_ID = "cargoId1"
private const val FILE_PATH = "app/cargo/cargoId1"

/**
 * Verifies the functionality of [ShuttleRoomDataModelFactory]. ShuttleRoomDataModelFactory
 * creates ShuttleRoomData instances from a cargo ID and file path, acting as the factory that
 * the warehouse uses to build index records before inserting them into the Room database. Without
 * it, the warehouse would have no way to produce the correct Room entity for persistence.
 */
class ShuttleRoomDataModelFactoryTest {
    private val modelFactory = ShuttleRoomDataModelFactory()

    @Test
    fun verifyCreationOfDataModel() {
        val model = modelFactory.createDataModel(cargoId = CARGO_ID, filePath = FILE_PATH)
        assertAll(
            { assertNotNull(model) },
            { assertTrue(model is ShuttleRoomData) },
            { assertEquals(CARGO_ID, model.cargoId) },
            { assertEquals(FILE_PATH, model.filePath) }
        )
    }
}
