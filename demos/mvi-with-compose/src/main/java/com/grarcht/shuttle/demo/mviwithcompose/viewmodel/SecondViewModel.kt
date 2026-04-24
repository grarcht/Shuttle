package com.grarcht.shuttle.demo.mviwithcompose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.mviwithcompose.intent.CargoPickupIntent
import com.grarcht.shuttle.demo.mviwithcompose.state.CargoPickupUiState
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.Serializable

private const val TAG = "SecondViewModel"
private const val WARN_EMPTY_CARGO = "Cargo is empty; no data was received for the given cargo ID."

/**
 * Processes MVI intents for the second view and emits the corresponding UI state
 * for cargo pickup via [CargoPickupIntent].
 */
class SecondViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CargoPickupUiState())
    val uiState: StateFlow<CargoPickupUiState> = _uiState.asStateFlow()

    fun processIntent(intent: CargoPickupIntent) {
        when (intent) {
            is CargoPickupIntent.LoadCargo -> loadCargo(intent)
        }
    }

    private fun loadCargo(intent: CargoPickupIntent.LoadCargo) {
        viewModelScope.launch {
            intent.shuttle
                .pickupCargo<Serializable>(cargoId = intent.cargoId)
                .consumeAsFlow()
                .collectLatest { result ->
                    when (result) {
                        is ShuttlePickupCargoResult.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is ShuttlePickupCargoResult.Success<*> -> {
                            if (result.data == null) {
                                Log.w(TAG, WARN_EMPTY_CARGO)
                            }
                            _uiState.update {
                                it.copy(isLoading = false, imageModel = result.data as? ImageModel)
                            }
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
