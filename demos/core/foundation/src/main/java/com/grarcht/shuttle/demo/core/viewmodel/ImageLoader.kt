package com.grarcht.shuttle.demo.core.viewmodel

import android.content.res.Resources
import androidx.annotation.RawRes
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for loading a raw-resource image and exposing the result as a [StateFlow] of [IOResult].
 */
interface ImageLoader {
    /** The current loading state, exposed as a [StateFlow] of [IOResult]. */
    val uiState: StateFlow<IOResult>

    /**
     * Initiates loading of the image identified by [imageId] from [resources]. Implementations
     * may skip repeated calls after a successful load.
     *
     * @param resources the app resources used to read the raw image file.
     * @param imageId the raw resource ID of the image to load.
     */
    fun loadImage(resources: Resources, @RawRes imageId: Int)

    /**
     * Returns the [ImageModel] from the last successful load, or null if loading has not
     * completed successfully.
     */
    fun currentImageModel(): ImageModel?
}
