@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel

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
 * Verifies the functionality of [SecondViewModel], including the initial
 * [ShuttlePickupCargoResult.NotPickingUpCargoYet] state exposed via [SecondViewModel.pickupCargoState]
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
    fun initialPickupCargoStateIsNotPickingUpCargoYet() {
        assertEquals(ShuttlePickupCargoResult.NotPickingUpCargoYet, viewModel.pickupCargoState.value)
    }

    @Test
    fun loadImageWithSuccessResultUpdatesPickupCargoState() = runTest {
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        val channel = Channel<ShuttlePickupCargoResult>()
        val shuttle = mock<Shuttle>()
        whenever(shuttle.pickupCargo<ImageModel>(any())).thenReturn(channel)

        viewModel.loadImage(shuttle, TEST_CARGO_ID)
        channel.send(ShuttlePickupCargoResult.Success(imageModel))

        assertAll(
            { assertTrue(viewModel.pickupCargoState.value is ShuttlePickupCargoResult.Success<*>) },
            { assertEquals(imageModel, (viewModel.pickupCargoState.value as ShuttlePickupCargoResult.Success<*>).data) }
        )
    }

    @Test
    fun loadImageWithErrorResultUpdatesPickupCargoState() = runTest {
        val throwable = Throwable("pickup failed")
        val channel = Channel<ShuttlePickupCargoResult>()
        val shuttle = mock<Shuttle>()
        whenever(shuttle.pickupCargo<ImageModel>(any())).thenReturn(channel)

        viewModel.loadImage(shuttle, TEST_CARGO_ID)
        channel.send(ShuttlePickupCargoResult.Error(TEST_CARGO_ID, throwable = throwable))

        assertAll(
            { assertTrue(viewModel.pickupCargoState.value is ShuttlePickupCargoResult.Error<*>) },
            { assertEquals(TEST_CARGO_ID, (viewModel.pickupCargoState.value as ShuttlePickupCargoResult.Error<*>).cargoId) }
        )
    }
}
