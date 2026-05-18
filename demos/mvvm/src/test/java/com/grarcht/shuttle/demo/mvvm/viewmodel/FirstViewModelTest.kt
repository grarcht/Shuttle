@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.grarcht.shuttle.demo.mvvm.viewmodel

import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

private const val TEST_CARGO_ID = "test-cargo-id"
private val TEST_IMAGE_DATA = byteArrayOf(1, 2, 3)

/**
 * Verifies the functionality of [FirstViewModel] and its associated navigation models, including
 * initial UI state, [NavigationCargo] value semantics, [NavigationEvent] construction, and the
 * suppression of navigation events when no image has been loaded.
 */
class FirstViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: FirstViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FirstViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiStateIsLoading() {
        assertTrue(viewModel.uiState.value is IOResult.Loading)
    }

    @Test
    fun navigationCargoHoldsCargoIdAndImageModel() {
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        val cargo = NavigationCargo(TEST_CARGO_ID, imageModel)
        assertAll(
            { assertEquals(TEST_CARGO_ID, cargo.cargoId) },
            { assertEquals(imageModel, cargo.imageModel) }
        )
    }

    @Test
    fun navigationCargoEqualityIsValueBased() {
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        val cargo1 = NavigationCargo(TEST_CARGO_ID, imageModel)
        val cargo2 = NavigationCargo(TEST_CARGO_ID, imageModel)
        assertEquals(cargo1, cargo2)
    }

    @Test
    fun navigationEventWithShuttleHoldsCargo() {
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        val cargo = NavigationCargo(TEST_CARGO_ID, imageModel)
        val event = NavigationEvent.WithShuttle(cargo)
        assertAll(
            { assertNotNull(event) },
            { assertEquals(cargo, event.cargo) }
        )
    }

    @Test
    fun navigationEventNormallyHoldsCargo() {
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        val cargo = NavigationCargo(TEST_CARGO_ID, imageModel)
        val event = NavigationEvent.Normally(cargo)
        assertAll(
            { assertNotNull(event) },
            { assertEquals(cargo, event.cargo) }
        )
    }

    @Test
    fun onNavigateWithShuttleEmitsNoEventWhenNoImageLoaded() = runTest {
        var eventReceived = false
        val job = launch(testDispatcher) {
            viewModel.navigationEvent.collect { eventReceived = true }
        }

        viewModel.onNavigateWithShuttle()
        advanceUntilIdle()

        assertFalse(eventReceived)
        job.cancel()
    }

    @Test
    fun onNavigateNormallyEmitsNoEventWhenNoImageLoaded() = runTest {
        var eventReceived = false
        val job = launch(testDispatcher) {
            viewModel.navigationEvent.collect { eventReceived = true }
        }

        viewModel.onNavigateNormally()
        advanceUntilIdle()

        assertFalse(eventReceived)
        job.cancel()
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.jupiter.api.Assertions.assertTrue(condition)
    }
}
