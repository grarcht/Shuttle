package com.grarcht.shuttle.framework.integrations.extensions.room

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

private const val CARGO_ID = "cargoId1"
private const val FILE_PATH = "/app/cargo/cargoId1"
private const val EMPTY_STRING = ""

/**
 * Verifies the [ShuttleRoomData] entity class constructors and field access.
 */
class ShuttleRoomDataTest {

    @Test
    fun verifyPrimaryConstructorCreatesDataWithCorrectFields() {
        val data = ShuttleRoomData(CARGO_ID, FILE_PATH)
        assertNotNull(data)
        assertEquals(CARGO_ID, data.cargoId)
        assertEquals(FILE_PATH, data.filePath)
    }

    @Test
    fun verifyNoArgConstructorCreatesDataWithEmptyFields() {
        val data = ShuttleRoomData()
        assertNotNull(data)
        assertEquals(EMPTY_STRING, data.cargoId)
        assertEquals(EMPTY_STRING, data.filePath)
    }

    @Test
    fun verifyFieldsCanBeUpdatedAfterConstruction() {
        val data = ShuttleRoomData()
        data.cargoId = CARGO_ID
        data.filePath = FILE_PATH
        assertEquals(CARGO_ID, data.cargoId)
        assertEquals(FILE_PATH, data.filePath)
    }
}
