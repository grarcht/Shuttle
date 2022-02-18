package com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.databinding.ObservableViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.coroutines.channel.relayFlowIfAvailable
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.Serializable

private const val LOAD_IMAGE_CHANNEL_CAPACITY = 2

/**
 * The MVVM ViewModel used with the Second View and corresponding model.
 */
class SecondViewModel : ObservableViewModel() {
    private val loadImageChannel = Channel<ShuttlePickupCargoResult>(LOAD_IMAGE_CHANNEL_CAPACITY)

    fun loadImage(shuttle: Shuttle, cargoId: String): Channel<ShuttlePickupCargoResult> {
        viewModelScope.launch {
            shuttle
                .pickupCargo<Serializable>(cargoId = cargoId)
                .relayFlowIfAvailable(loadImageChannel)
        }

        return loadImageChannel
    }
}
