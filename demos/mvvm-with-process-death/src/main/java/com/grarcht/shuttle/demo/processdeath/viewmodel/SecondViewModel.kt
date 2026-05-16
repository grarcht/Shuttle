package com.grarcht.shuttle.demo.processdeath.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageModel
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

class SecondViewModel : ViewModel() {

    private val _imageState = MutableStateFlow<SecondImageState>(SecondImageState.Loading)
    val imageState: StateFlow<SecondImageState> = _imageState.asStateFlow()

    fun loadFromShuttle(shuttle: Shuttle, cargoId: String) {
        if (cargoId.isEmpty()) {
            _imageState.value = SecondImageState.LostToProcessDeath
            return
        }
        viewModelScope.launch {
            shuttle.pickupCargo<ImageModel>(cargoId = cargoId)
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

    fun loadFromMemoryCache() {
        val model = ImageCache.imageModel
        _imageState.value = if (model != null) {
            SecondImageState.Success(model)
        } else {
            SecondImageState.LostToProcessDeath
        }
    }

    fun currentImageModel(): ImageModel? =
        (_imageState.value as? SecondImageState.Success)?.imageModel
}
