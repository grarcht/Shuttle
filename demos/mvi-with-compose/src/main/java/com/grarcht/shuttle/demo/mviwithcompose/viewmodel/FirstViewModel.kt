package com.grarcht.shuttle.demo.mviwithcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.io.RawResourceGateway
import com.grarcht.shuttle.demo.mviwithcompose.intent.CargoTransportIntent
import com.grarcht.shuttle.demo.mviwithcompose.navigation.NavigationEvent
import com.grarcht.shuttle.demo.mviwithcompose.state.CargoTransportUiState
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Processes MVI intents for the first view and emits the corresponding UI state
 * for image loading via [CargoTransportIntent]. Navigation decisions are emitted
 * as one-shot [NavigationEvent]s for the view to execute.
 *
 * @param shuttle the Shuttle instance used for cargo cleanup.
 */
@HiltViewModel
class FirstViewModel @Inject constructor(
    private val shuttle: Shuttle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CargoTransportUiState())
    val uiState: StateFlow<CargoTransportUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvent: Flow<NavigationEvent> = _navigationEvent.receiveAsFlow()

    fun processIntent(intent: CargoTransportIntent) {
        when (intent) {
            is CargoTransportIntent.LoadImage -> loadImage(intent)
            is CargoTransportIntent.NavigateWithShuttle -> emitNavigationEvent(
                NavigationEvent.NavigateWithShuttle(intent.imageModel)
            )
            is CargoTransportIntent.NavigateNormally -> emitNavigationEvent(
                NavigationEvent.NavigateNormally(intent.imageModel)
            )
            is CargoTransportIntent.CleanUp -> cleanUp()
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

    private fun emitNavigationEvent(event: NavigationEvent) {
        viewModelScope.launch {
            _navigationEvent.send(event)
        }
    }

    fun cleanUp() {
        shuttle.cleanShuttleFromAllDeliveries()
    }
}
