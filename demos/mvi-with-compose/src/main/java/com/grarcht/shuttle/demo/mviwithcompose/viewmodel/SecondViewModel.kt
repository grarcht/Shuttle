package com.grarcht.shuttle.demo.mviwithcompose.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.mviwithcompose.intent.CargoPickupIntent
import com.grarcht.shuttle.demo.mviwithcompose.state.CargoPickupUiState
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

private const val TAG = "SecondViewModel"
private const val WARN_EMPTY_CARGO = "Cargo is empty; no data was received for the given cargo ID."
private const val WARN_UNEXPECTED_CARGO_TYPE = "Cargo data is not an ImageModel; the type is unexpected."

/**
 * Processes MVI intents for the second view and emits the corresponding UI state
 * for cargo pickup via [CargoPickupIntent].
 *
 * @param shuttle the Shuttle instance used for cargo pickup and instance state bundling.
 */
@HiltViewModel
class SecondViewModel @Inject constructor(
    private val shuttle: Shuttle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CargoPickupUiState())
    val uiState: StateFlow<CargoPickupUiState> = _uiState.asStateFlow()

    fun processIntent(intent: CargoPickupIntent) {
        when (intent) {
            is CargoPickupIntent.LoadCargo -> loadCargo(intent)
        }
    }

    fun buildSavedInstanceState(outState: Bundle): Bundle {
        return shuttle
            .bundleCargoWith(outState)
            .logTag(TAG)
            .transport(ImageMessageType.ImageData.value, uiState.value.imageModel)
            .create()
    }

    private fun loadCargo(intent: CargoPickupIntent.LoadCargo) {
        viewModelScope.launch {
            shuttle
                .pickupCargo<Serializable>(cargoId = intent.cargoId)
                .consumeAsFlow()
                .collectLatest { result ->
                    when (result) {
                        is ShuttlePickupCargoResult.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is ShuttlePickupCargoResult.Success<*> -> {
                            val imageModel = result.data as? ImageModel
                            when {
                                result.data == null -> Log.w(TAG, WARN_EMPTY_CARGO)
                                imageModel == null -> Log.w(TAG, WARN_UNEXPECTED_CARGO_TYPE)
                            }
                            _uiState.update { it.copy(isLoading = false, imageModel = imageModel) }
                            cancel()
                        }
                        is ShuttlePickupCargoResult.Error<*> -> {
                            _uiState.update {
                                it.copy(isLoading = false, error = result.throwable as Throwable)
                            }
                            cancel()
                        }
                        is ShuttlePickupCargoResult.NotPickingUpCargoYet -> { /* ignore */ }
                    }
                }
        }
    }
}
