package com.grarcht.shuttle.demo.mvvmwithnavigation.viewmodel

import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.databinding.ObservableViewModel
import com.grarcht.shuttle.demo.mvvmwithnavigation.BR
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.io.Serializable

/**
 * The MVVM ViewModel used with the Second View and corresponding model.
 */
class SecondViewModel : ObservableViewModel() {
    var imageModel: ImageModel? = null

    @get:Bindable
    var shuttlePickupCargoResult: ShuttlePickupCargoResult? = null

    fun loadImage(shuttle: Shuttle, cargoId: String) {
        viewModelScope.launch {
            shuttle.pickupCargo<Serializable>(cargoId = cargoId)
                .consumeAsFlow()
                .collectLatest { shuttleResult ->
                    shuttlePickupCargoResult = shuttleResult

                    when (shuttleResult) {
                        ShuttlePickupCargoResult.Loading -> {
                            notifyPropertyChanged(BR.shuttlePickupCargoResult)
                        }
                        is ShuttlePickupCargoResult.Success<*> -> {
                            imageModel = shuttleResult.data as ImageModel
                            notifyPropertyChanged(BR.shuttlePickupCargoResult)
                            cancel()
                        }
                        is ShuttlePickupCargoResult.Error<*> -> {
                            notifyPropertyChanged(BR.shuttlePickupCargoResult)
                            cancel()
                        }
                        else -> {
                            // ignore
                        }
                    }
                }
        }
    }
}
