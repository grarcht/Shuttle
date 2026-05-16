package com.grarcht.shuttle.demo.mvvm.viewmodel

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
    private val pickupCargoMutableStateFlow = MutableStateFlow<ShuttlePickupCargoResult>(NotPickingUpCargoYet)
    private val pickupCargoStateFlow: StateFlow<ShuttlePickupCargoResult> = pickupCargoMutableStateFlow

    fun loadImage(shuttle: Shuttle, cargoId: String): StateFlow<ShuttlePickupCargoResult> {
        viewModelScope.launch {
            shuttle.pickupCargo<ImageModel>(cargoId = cargoId)
                .consumeAsFlow()
                .collectLatest { shuttleResult ->
                    pickupCargoMutableStateFlow.value = shuttleResult

                    when (shuttleResult) {
                        is ShuttlePickupCargoResult.Success<*>,
                        is ShuttlePickupCargoResult.Error<*> -> {
                            cancel()
                        }
                        else -> {
                            // ignore
                        }
                    }
                }
        }

        return pickupCargoStateFlow
    }
}
