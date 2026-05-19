package com.grarcht.shuttle.demo.mviwithcompose.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.shuttle.CARGO_PICKUP_TIMEOUT_MS
import com.grarcht.shuttle.demo.mviwithcompose.intent.CargoPickupIntent
import com.grarcht.shuttle.demo.mviwithcompose.state.CargoPickupUiState
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ERROR_UNKNOWN = "Unknown error"
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

    /**
     * Processes the given [intent] and updates [uiState] with the cargo pickup result. This is
     * the single entry point for all actions on the second view.
     *
     * @param intent the action to process.
     */
    fun processIntent(intent: CargoPickupIntent) {
        when (intent) {
            is CargoPickupIntent.LoadCargo -> loadCargo(intent)
        }
    }

    /**
     * Bundles the currently retrieved image model into [outState] using Shuttle so it can be
     * restored safely after a configuration change.
     *
     * @param outState the bundle to write the cargo into.
     * @return the bundle with the cargo added.
     */
    fun buildSavedInstanceState(outState: Bundle): Bundle {
        return shuttle
            .bundleCargoWith(outState)
            .logTag(TAG)
            .transport(IMAGE_CARGO_ID, uiState.value.imageModel)
            .create()
    }

    private fun loadCargo(intent: CargoPickupIntent.LoadCargo) {
        viewModelScope.launch {
            shuttle
                .pickupCargo<ImageModel>(cargoId = intent.cargoId, timeoutMs = CARGO_PICKUP_TIMEOUT_MS)
                .consumeAsFlow()
                .collect { result ->
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
                        }
                        is ShuttlePickupCargoResult.Error<*> -> {
                            _uiState.update {
                                it.copy(isLoading = false, error = result.throwable ?: Throwable(ERROR_UNKNOWN))
                            }
                        }
                        is ShuttlePickupCargoResult.NotPickingUpCargoYet -> { /* ignore */ }
                    }
                }
        }
    }
}
