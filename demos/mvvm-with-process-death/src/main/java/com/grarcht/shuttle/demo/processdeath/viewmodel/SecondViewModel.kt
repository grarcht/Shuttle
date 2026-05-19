package com.grarcht.shuttle.demo.processdeath.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.shuttle.CARGO_PICKUP_TIMEOUT_MS
import com.grarcht.shuttle.demo.processdeath.model.ImageCache
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

private const val ERROR_FAILED_TO_RETRIEVE_CARGO = "Failed to retrieve cargo."
private const val ERROR_UNEXPECTED_DATA_TYPE = "Unexpected data type received."

/**
 * The MVVM [androidx.lifecycle.ViewModel] for the second screen in the MVVM with Process Death
 * demo. Loads the image either from the [com.grarcht.shuttle.framework.Shuttle] warehouse or
 * from the in-memory [com.grarcht.shuttle.demo.processdeath.model.ImageCache], and exposes the
 * result as a [StateFlow] of [SecondImageState].
 */
class SecondViewModel : ViewModel() {

    private val _imageState = MutableStateFlow<SecondImageState>(SecondImageState.Loading)
    val imageState: StateFlow<SecondImageState> = _imageState.asStateFlow()

    /**
     * Picks up cargo from the [com.grarcht.shuttle.framework.Shuttle] warehouse using [cargoId]
     * and updates [imageState] with the result. Transitions to [SecondImageState.LostToProcessDeath]
     * immediately when [cargoId] is empty, indicating that the cargo was not preserved.
     *
     * @param shuttle the Shuttle instance used for cargo pickup.
     * @param cargoId the identifier of the cargo to retrieve, or an empty string if lost.
     */
    fun loadFromShuttle(shuttle: Shuttle, cargoId: String) {
        if (cargoId.isEmpty()) {
            _imageState.value = SecondImageState.LostToProcessDeath
            return
        }
        viewModelScope.launch {
            shuttle.pickupCargo<ImageModel>(cargoId = cargoId, timeoutMs = CARGO_PICKUP_TIMEOUT_MS)
                .consumeAsFlow()
                .collectLatest { result ->
                    when (result) {
                        is ShuttlePickupCargoResult.Success<*> -> {
                            val imageModel = result.data as? ImageModel ?: run {
                                _imageState.value = SecondImageState.Error(ERROR_UNEXPECTED_DATA_TYPE)
                                cancel()
                                return@collectLatest
                            }
                            _imageState.value = SecondImageState.Success(imageModel)
                            cancel()
                        }
                        is ShuttlePickupCargoResult.Error<*> -> {
                            _imageState.value = SecondImageState.Error(
                                result.throwable?.message ?: ERROR_FAILED_TO_RETRIEVE_CARGO
                            )
                            cancel()
                        }
                        else -> { /* loading — ignore */ }
                    }
                }
        }
    }

    /**
     * Loads the image from the in-memory [com.grarcht.shuttle.demo.processdeath.model.ImageCache]
     * and updates [imageState]. Transitions to [SecondImageState.LostToProcessDeath] if the cache
     * is empty, demonstrating that in-memory state does not survive process death.
     */
    fun loadFromMemoryCache() {
        val model = ImageCache.imageModel
        _imageState.value = if (model != null) {
            SecondImageState.Success(model)
        } else {
            SecondImageState.LostToProcessDeath
        }
    }

    /**
     * Returns the [com.grarcht.shuttle.demo.core.image.ImageModel] from the current
     * [SecondImageState.Success] state, or null if the image has not been loaded successfully.
     */
    fun currentImageModel(): ImageModel? =
        (_imageState.value as? SecondImageState.Success)?.imageModel
}
