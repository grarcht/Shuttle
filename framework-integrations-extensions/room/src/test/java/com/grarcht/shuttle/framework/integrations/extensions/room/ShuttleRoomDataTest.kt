package com.grarcht.shuttle.framework.integrations.extensions.room

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

private const val CARGO_ID = "cargoId1"
private const val FILE_PATH = "/app/cargo/cargoId1"
private const val EMPTY_STRING = ""

/**
 * Verifies the functionality of [ShuttleRoomData]. ShuttleRoomData is the Room entity that maps
 * a cargo ID to its file path on disk, forming the index record stored in the database for every
 * transported payload. If its fields were not correctly readable and writable, the persistence
 * layer could not locate the serialized files for pickup or deletion.
 */
class ShuttleRoomDataTest {

    @Test
    fun verifyPrimaryConstructorCreatesDataWithCorrectFields() {
        val data = ShuttleRoomData(CARGO_ID, FILE_PATH)
        assertAll(
            { assertNotNull(data) },
            { assertEquals(CARGO_ID, data.cargoId) },
            { assertEquals(FILE_PATH, data.filePath) }
        )
    }

    @Test
    fun verifyNoArgConstructorCreatesDataWithEmptyFields() {
        val data = ShuttleRoomData()
        assertAll(
            { assertNotNull(data) },
            { assertEquals(EMPTY_STRING, data.cargoId) },
            { assertEquals(EMPTY_STRING, data.filePath) }
        )
    }

    @Test
    fun verifyFieldsCanBeUpdatedAfterConstruction() {
        val data = ShuttleRoomData()
        data.cargoId = CARGO_ID
        data.filePath = FILE_PATH
        assertAll(
            { assertEquals(CARGO_ID, data.cargoId) },
            { assertEquals(FILE_PATH, data.filePath) }
        )
    }
}
