package com.grarcht.shuttle.demo.mviwithcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.io.RawResourceGateway
import com.grarcht.shuttle.demo.mviwithcompose.intent.CargoTransportIntent
import com.grarcht.shuttle.demo.mviwithcompose.state.CargoTransportUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Processes MVI intents for the first view and emits the corresponding UI state
 * for image loading via [CargoTransportIntent].
 */
class FirstViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CargoTransportUiState())
    val uiState: StateFlow<CargoTransportUiState> = _uiState.asStateFlow()

    fun processIntent(intent: CargoTransportIntent) {
        when (intent) {
            is CargoTransportIntent.LoadImage -> loadImage(intent)
        }
    }

    private fun loadImage(intent: CargoTransportIntent.LoadImage) {
        viewModelScope.launch {
            RawResourceGateway.with(intent.resources)
                .bytesFromRawResource(intent.imageId)
                .create()
                .collectLatest { result ->
                    when (result) {
                        is IOResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is IOResult.Success<*> -> {
                            val bytes = result.data as ByteArray
                            val model = ImageModel(ImageMessageType.ImageData.value, bytes)
                            _uiState.update { it.copy(isLoading = false, imageModel = model) }
                        }
                        is IOResult.Error<*> -> {
                            _uiState.update { it.copy(isLoading = false, error = result.throwable) }
                        }
                        is IOResult.Unknown -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                }
        }
    }
}
