package com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult.NotPickingUpCargoYet
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

/**
 * The MVVM ViewModel used with the Second View and corresponding model.
 */
class SecondViewModel : ViewModel() {

    private val _pickupCargoState = MutableStateFlow<ShuttlePickupCargoResult>(NotPickingUpCargoYet)
    val pickupCargoState: StateFlow<ShuttlePickupCargoResult> = _pickupCargoState

    fun loadImage(shuttle: Shuttle, cargoId: String) {
        viewModelScope.launch {
            shuttle.pickupCargo<ImageModel>(cargoId = cargoId)
                .consumeAsFlow()
                .collectLatest { result ->
                    _pickupCargoState.value = result
                    when (result) {
                        is ShuttlePickupCargoResult.Success<*>,
                        is ShuttlePickupCargoResult.Error<*> -> cancel()
                        else -> { /* ignore */ }
                    }
                }
        }
    }
}
