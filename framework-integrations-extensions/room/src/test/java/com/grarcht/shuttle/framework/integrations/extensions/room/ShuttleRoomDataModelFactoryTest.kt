package com.grarcht.shuttle.framework.integrations.extensions.room

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val CARGO_ID = "cargoId1"
private const val FILE_PATH = "app/cargo/cargoId1"

/**
 * Verifies the [ShuttleRoomDataModelFactory] functionality.
 */
class ShuttleRoomDataModelFactoryTest {
    private val modelFactory = ShuttleRoomDataModelFactory()

    @Test
    fun verifyCreationOfDataModel() {
        val model = modelFactory.createDataModel(cargoId = CARGO_ID, filePath = FILE_PATH)
        assertNotNull(model)
        assertTrue(model is ShuttleRoomData)
        assertEquals(CARGO_ID, model.cargoId)
        assertEquals(FILE_PATH, model.filePath)
    }
}
