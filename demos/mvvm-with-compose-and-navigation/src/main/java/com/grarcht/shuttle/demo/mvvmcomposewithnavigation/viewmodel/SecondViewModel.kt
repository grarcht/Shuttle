package com.grarcht.shuttle.demo.mvvmcomposewithnavigation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult.NotPickingUpCargoYet
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult.Success
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

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

    fun currentImageModel(): ImageModel? =
        (_pickupCargoState.value as? Success<*>)?.data as? ImageModel
}
