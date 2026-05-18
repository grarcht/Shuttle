package com.grarcht.shuttle.demo.mvvm.viewmodel

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.viewmodel.DefaultImageLoader
import com.grarcht.shuttle.demo.core.viewmodel.ImageLoader
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class NavigationCargo(val cargoId: String, val imageModel: ImageModel)

sealed class NavigationEvent {
    data class WithShuttle(val cargo: NavigationCargo) : NavigationEvent()
    data class Normally(val cargo: NavigationCargo) : NavigationEvent()
}

class FirstViewModel : ViewModel() {

    private val imageLoader: ImageLoader = DefaultImageLoader(viewModelScope)
    val uiState: StateFlow<IOResult> = imageLoader.uiState

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    fun loadImage(resources: Resources, @RawRes imageId: Int) = imageLoader.loadImage(resources, imageId)

    fun currentImageModel(): ImageModel? = imageLoader.currentImageModel()

    fun onNavigateWithShuttle() {
        val cargo = currentImageModel()?.let { NavigationCargo(IMAGE_CARGO_ID, it) } ?: return
        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.WithShuttle(cargo)) }
    }

    fun onNavigateNormally() {
        val cargo = currentImageModel()?.let { NavigationCargo(IMAGE_CARGO_ID, it) } ?: return
        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.Normally(cargo)) }
    }
}
