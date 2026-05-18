@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.grarcht.shuttle.demo.mvvm.viewmodel

import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private val TEST_IMAGE_DATA = byteArrayOf(1, 2, 3)
private const val TEST_CARGO_ID = "test-cargo-id"

/**
 * Verifies the functionality of [SecondViewModel], including the state flow returned by
 * [SecondViewModel.loadImage], its initial [ShuttlePickupCargoResult.NotPickingUpCargoYet] state,
 * and state updates upon receiving success and error results from [Shuttle] cargo pickup.
 */
class SecondViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: SecondViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SecondViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadImageReturnsNonNullStateFlow() = runTest {
        val shuttle = mock<Shuttle>()
        val channel = Channel<ShuttlePickupCargoResult>()
        whenever(shuttle.pickupCargo<ImageModel>(any())).thenReturn(channel)

        val stateFlow = viewModel.loadImage(shuttle, TEST_CARGO_ID)
        assertNotNull(stateFlow)
    }

    @Test
    fun loadImageInitialStateIsNotPickingUpCargoYet() = runTest {
        val shuttle = mock<Shuttle>()
        val channel = Channel<ShuttlePickupCargoResult>()
        whenever(shuttle.pickupCargo<ImageModel>(any())).thenReturn(channel)

        val stateFlow = viewModel.loadImage(shuttle, TEST_CARGO_ID)
        assertEquals(ShuttlePickupCargoResult.NotPickingUpCargoYet, stateFlow.value)
    }

    @Test
    fun loadImageWithSuccessResultUpdatesStateFlow() = runTest {
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        val channel = Channel<ShuttlePickupCargoResult>()
        val shuttle = mock<Shuttle>()
        whenever(shuttle.pickupCargo<ImageModel>(any())).thenReturn(channel)

        val stateFlow = viewModel.loadImage(shuttle, TEST_CARGO_ID)
        channel.send(ShuttlePickupCargoResult.Success(imageModel))

        assertAll(
            { assertTrue(stateFlow.value is ShuttlePickupCargoResult.Success<*>) },
            { assertEquals(imageModel, (stateFlow.value as ShuttlePickupCargoResult.Success<*>).data) }
        )
    }

    @Test
    fun loadImageWithErrorResultUpdatesStateFlow() = runTest {
        val throwable = Throwable("pickup failed")
        val channel = Channel<ShuttlePickupCargoResult>()
        val shuttle = mock<Shuttle>()
        whenever(shuttle.pickupCargo<ImageModel>(any())).thenReturn(channel)

        val stateFlow = viewModel.loadImage(shuttle, TEST_CARGO_ID)
        channel.send(ShuttlePickupCargoResult.Error(TEST_CARGO_ID, throwable = throwable))

        assertAll(
            { assertTrue(stateFlow.value is ShuttlePickupCargoResult.Error<*>) },
            { assertEquals(TEST_CARGO_ID, (stateFlow.value as ShuttlePickupCargoResult.Error<*>).cargoId) }
        )
    }
}
