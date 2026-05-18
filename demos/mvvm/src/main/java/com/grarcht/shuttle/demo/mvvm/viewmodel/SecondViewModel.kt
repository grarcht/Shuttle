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
 * The MVVM [androidx.lifecycle.ViewModel] for the second screen in the MVVM demo. Picks up cargo
 * from the [com.grarcht.shuttle.framework.Shuttle] warehouse and exposes the result as a
 * [StateFlow] of [com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult].
 */
class SecondViewModel : ViewModel() {
    private val pickupCargoMutableStateFlow = MutableStateFlow<ShuttlePickupCargoResult>(NotPickingUpCargoYet)
    private val pickupCargoStateFlow: StateFlow<ShuttlePickupCargoResult> = pickupCargoMutableStateFlow

    /**
     * Initiates cargo pickup from the [com.grarcht.shuttle.framework.Shuttle] warehouse using
     * [cargoId] and returns a [StateFlow] that emits the result. The flow terminates automatically
     * upon a success or error result.
     *
     * @param shuttle the Shuttle instance used for cargo pickup.
     * @param cargoId the identifier of the cargo to retrieve from the warehouse.
     */
    fun loadImage(shuttle: Shuttle, cargoId: String): StateFlow<ShuttlePickupCargoResult> {
        viewModelScope.launch {
            shuttle.pickupCargo<ImageModel>(cargoId = cargoId)
                .consumeAsFlow()
                .collectLatest { shuttleResult ->
                    pickupCargoMutableStateFlow.value = shuttleResult
                    when (shuttleResult) {
                        is ShuttlePickupCargoResult.Success<*>,
                        is ShuttlePickupCargoResult.Error<*> -> cancel()
                        else -> { /* ignore */ }
                    }
                }
        }

        return pickupCargoStateFlow
    }
}
