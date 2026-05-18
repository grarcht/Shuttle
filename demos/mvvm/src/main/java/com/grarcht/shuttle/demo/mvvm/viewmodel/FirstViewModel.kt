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

/**
 * Bundles a cargo identifier with the [ImageModel] that should be transported to the second screen.
 *
 * @property cargoId the key used to store and retrieve the cargo in the Shuttle warehouse.
 * @property imageModel the image data to transport.
 */
data class NavigationCargo(val cargoId: String, val imageModel: ImageModel)

/**
 * One-shot navigation events emitted by [FirstViewModel] to the view. The view collects these and
 * executes the corresponding platform navigation.
 */
sealed class NavigationEvent {
    /**
     * Signals that the view should navigate by transporting the cargo safely via Shuttle.
     *
     * @property cargo the image model and cargo ID to transport.
     */
    data class WithShuttle(val cargo: NavigationCargo) : NavigationEvent()

    /**
     * Signals that the view should navigate by passing the image model directly in an
     * [android.content.Intent], demonstrating the crash scenario.
     *
     * @property cargo the image model and cargo ID to pass directly.
     */
    data class Normally(val cargo: NavigationCargo) : NavigationEvent()
}

/**
 * The MVVM [androidx.lifecycle.ViewModel] for the first screen in the MVVM demo. Delegates image
 * loading to a [com.grarcht.shuttle.demo.core.viewmodel.DefaultImageLoader] and emits one-shot
 * [NavigationEvent]s when the user requests navigation.
 */
class FirstViewModel : ViewModel() {

    private val imageLoader: ImageLoader = DefaultImageLoader(viewModelScope)
    val uiState: StateFlow<IOResult> = imageLoader.uiState

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    /**
     * Loads the image from [imageId] in [resources]. Repeated calls after a successful load
     * are ignored.
     *
     * @param resources the app resources used to read the raw image file.
     * @param imageId the raw resource ID of the image to load.
     */
    fun loadImage(resources: Resources, @RawRes imageId: Int) = imageLoader.loadImage(resources, imageId)

    /**
     * Returns the [ImageModel] from the last successful load, or null if loading has not
     * completed successfully.
     */
    fun currentImageModel(): ImageModel? = imageLoader.currentImageModel()

    /**
     * Emits a [NavigationEvent.WithShuttle] event for the view to execute, carrying the currently
     * loaded image model. Does nothing if no image has been loaded yet.
     */
    fun onNavigateWithShuttle() {
        val cargo = currentImageModel()?.let { NavigationCargo(IMAGE_CARGO_ID, it) } ?: return
        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.WithShuttle(cargo)) }
    }

    /**
     * Emits a [NavigationEvent.Normally] event for the view to execute, carrying the currently
     * loaded image model. Does nothing if no image has been loaded yet.
     */
    fun onNavigateNormally() {
        val cargo = currentImageModel()?.let { NavigationCargo(IMAGE_CARGO_ID, it) } ?: return
        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.Normally(cargo)) }
    }
}
