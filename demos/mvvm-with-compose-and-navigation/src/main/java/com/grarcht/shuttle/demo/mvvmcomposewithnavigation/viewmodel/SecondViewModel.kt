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

/**
 * The MVVM [androidx.lifecycle.ViewModel] for the second screen in the MVVM with Compose and
 * Navigation demo. Picks up cargo from the [com.grarcht.shuttle.framework.Shuttle] warehouse
 * and exposes the result as a [StateFlow] of
 * [com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult].
 */
class SecondViewModel : ViewModel() {

    private val _pickupCargoState = MutableStateFlow<ShuttlePickupCargoResult>(NotPickingUpCargoYet)
    val pickupCargoState: StateFlow<ShuttlePickupCargoResult> = _pickupCargoState

    /**
     * Initiates cargo pickup from the [com.grarcht.shuttle.framework.Shuttle] warehouse using
     * [cargoId] and updates [pickupCargoState] with the result. The pickup terminates
     * automatically upon a success or error result.
     *
     * @param shuttle the Shuttle instance used for cargo pickup.
     * @param cargoId the identifier of the cargo to retrieve from the warehouse.
     */
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

    /**
     * Returns the [com.grarcht.shuttle.demo.core.image.ImageModel] from the last successful
     * pickup, or null if pickup has not completed successfully.
     */
    fun currentImageModel(): ImageModel? =
        (_pickupCargoState.value as? Success<*>)?.data as? ImageModel
}
