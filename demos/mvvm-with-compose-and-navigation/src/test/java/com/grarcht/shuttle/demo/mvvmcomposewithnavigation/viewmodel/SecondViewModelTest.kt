package com.grarcht.shuttle.demo.mvvmcomposewithnavigation.viewmodel

import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

private const val TEST_CARGO_ID = "test-cargo-id"
private val TEST_IMAGE_DATA = byteArrayOf(1, 2, 3)

/**
 * Verifies the functionality of [SecondViewModel], including the initial
 * [ShuttlePickupCargoResult.NotPickingUpCargoYet] state and the [SecondViewModel.currentImageModel]
 * accessor for both the null and success states.
 */
class SecondViewModelTest {

    @Test
    fun initialPickupCargoStateIsNotPickingUpCargoYet() {
        val viewModel = SecondViewModel()
        assertEquals(ShuttlePickupCargoResult.NotPickingUpCargoYet, viewModel.pickupCargoState.value)
    }

    @Test
    fun currentImageModelReturnsNullWhenStateIsNotPickingUpCargoYet() {
        val viewModel = SecondViewModel()
        assertNull(viewModel.currentImageModel())
    }

    @Test
    fun currentImageModelReturnsModelWhenStateIsSuccess() {
        val viewModel = SecondViewModel()
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        val field = viewModel.javaClass.getDeclaredField("_pickupCargoState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<ShuttlePickupCargoResult>
        stateFlow.value = ShuttlePickupCargoResult.Success(imageModel)

        assertEquals(imageModel, viewModel.currentImageModel())
    }
}
