package com.grarcht.shuttle.demo.mviwithcompose.state

import com.grarcht.shuttle.demo.core.image.ImageModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

private const val TEST_CARGO_ID = "test-cargo-id"
private val TEST_IMAGE_DATA = byteArrayOf(1, 2, 3)

/**
 * Verifies the functionality of [CargoPickupUiState], including default property values,
 * copy-based state updates for loading, image model, and error fields, and value-based
 * equality between state instances.
 */
class CargoPickupUiStateTest {

    @Test
    fun defaultStateHasExpectedValues() {
        val state = CargoPickupUiState()
        assertAll(
            { assertTrue(state.isLoading) },
            { assertNull(state.imageModel) },
            { assertNull(state.error) }
        )
    }

    @Test
    fun copyUpdatesIsLoading() {
        val state = CargoPickupUiState().copy(isLoading = false)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun copyUpdatesImageModel() {
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        val state = CargoPickupUiState().copy(imageModel = imageModel)
        assertEquals(imageModel, state.imageModel)
    }

    @Test
    fun copyUpdatesError() {
        val throwable = Throwable("test error")
        val state = CargoPickupUiState().copy(error = throwable)
        assertEquals(throwable, state.error)
    }

    @Test
    fun equalStatesAreEqual() {
        val state1 = CargoPickupUiState(isLoading = false)
        val state2 = CargoPickupUiState(isLoading = false)
        assertEquals(state1, state2)
    }
}
