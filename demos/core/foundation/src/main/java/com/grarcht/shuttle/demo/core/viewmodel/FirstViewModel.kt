package com.grarcht.shuttle.demo.core.viewmodel

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import kotlinx.coroutines.flow.StateFlow

/**
 * The shared [androidx.lifecycle.ViewModel] for the first screen in each demo. Delegates image
 * loading to a [DefaultImageLoader] and exposes the resulting [IOResult] state as a [StateFlow].
 */
class FirstViewModel : ViewModel() {

    private val imageLoader: ImageLoader = DefaultImageLoader(viewModelScope)
    val uiState: StateFlow<IOResult> = imageLoader.uiState

    /**
     * Loads the image from [imageId] in [resources] and updates [uiState] with the result.
     * Calls that arrive after a successful load are ignored.
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
}
