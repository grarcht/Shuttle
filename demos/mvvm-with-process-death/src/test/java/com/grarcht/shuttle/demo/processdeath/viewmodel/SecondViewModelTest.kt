@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.grarcht.shuttle.demo.processdeath.viewmodel

import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.processdeath.model.ImageCache
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
import org.junit.jupiter.api.Assertions.assertNull
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
 * Verifies the functionality of [SecondViewModel], including initial image state, loading from
 * the in-memory [ImageCache], loading via [Shuttle] cargo pickup with success and error results,
 * process-death fallback behavior, and the [SecondViewModel.currentImageModel] accessor.
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
        ImageCache.imageModel = null
    }

    @Test
    fun initialStateIsLoading() {
        assertTrue(viewModel.imageState.value is SecondImageState.Loading)
    }

    @Test
    fun loadFromShuttleWithEmptyCargoIdSetsLostToProcessDeath() {
        viewModel.loadFromShuttle(mock(), "")
        assertEquals(SecondImageState.LostToProcessDeath, viewModel.imageState.value)
    }

    @Test
    fun loadFromMemoryCacheWhenCacheIsNullSetsLostToProcessDeath() {
        ImageCache.imageModel = null
        viewModel.loadFromMemoryCache()
        assertEquals(SecondImageState.LostToProcessDeath, viewModel.imageState.value)
    }

    @Test
    fun loadFromMemoryCacheWhenCacheHasModelSetsSuccess() {
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        ImageCache.imageModel = imageModel
        viewModel.loadFromMemoryCache()
        assertEquals(SecondImageState.Success(imageModel), viewModel.imageState.value)
    }

    @Test
    fun currentImageModelReturnsNullWhenStateIsLoading() {
        assertNull(viewModel.currentImageModel())
    }

    @Test
    fun currentImageModelReturnsModelWhenStateIsSuccess() {
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        ImageCache.imageModel = imageModel
        viewModel.loadFromMemoryCache()
        assertEquals(imageModel, viewModel.currentImageModel())
    }

    @Test
    fun loadFromShuttleWithSuccessResultSetsSuccess() = runTest {
        val imageModel = ImageModel(TEST_CARGO_ID, TEST_IMAGE_DATA)
        val channel = Channel<ShuttlePickupCargoResult>()
        val shuttle = mock<Shuttle>()
        whenever(shuttle.pickupCargo<ImageModel>(any(), any())).thenReturn(channel)

        viewModel.loadFromShuttle(shuttle, TEST_CARGO_ID)
        channel.send(ShuttlePickupCargoResult.Success(imageModel))

        assertEquals(SecondImageState.Success(imageModel), viewModel.imageState.value)
    }

    @Test
    fun loadFromShuttleWithErrorResultSetsError() = runTest {
        val throwable = Throwable("pickup failed")
        val channel = Channel<ShuttlePickupCargoResult>()
        val shuttle = mock<Shuttle>()
        whenever(shuttle.pickupCargo<ImageModel>(any(), any())).thenReturn(channel)

        viewModel.loadFromShuttle(shuttle, TEST_CARGO_ID)
        channel.send(ShuttlePickupCargoResult.Error(TEST_CARGO_ID, throwable = throwable))

        assertAll(
            { assertTrue(viewModel.imageState.value is SecondImageState.Error) },
            { assertEquals("pickup failed", (viewModel.imageState.value as SecondImageState.Error).message) }
        )
    }

    @Test
    fun loadFromShuttleWithUnexpectedDataTypeSetsError() = runTest {
        val channel = Channel<ShuttlePickupCargoResult>()
        val shuttle = mock<Shuttle>()
        whenever(shuttle.pickupCargo<ImageModel>(any(), any())).thenReturn(channel)

        viewModel.loadFromShuttle(shuttle, TEST_CARGO_ID)
        channel.send(ShuttlePickupCargoResult.Success("not-an-image-model"))

        assertTrue(viewModel.imageState.value is SecondImageState.Error)
    }
}
