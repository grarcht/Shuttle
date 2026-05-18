package com.grarcht.shuttle.demo.mviwithcompose.state

import com.grarcht.shuttle.demo.core.image.ImageModel

/**
 * Holds the UI state for the first view during cargo transport. The state reflects
 * whether the image is still loading, has been loaded successfully, or encountered
 * an error. Navigation buttons are enabled only when an image model is available.
 *
 * @property isLoading true while the image is being read from the raw resource.
 * @property imageModel the loaded image data, or null if loading has not completed.
 * @property error the throwable captured when loading fails, or null on success.
 */
data class CargoTransportUiState(
    val isLoading: Boolean = true,
    val imageModel: ImageModel? = null,
    val error: Throwable? = null
) {
    val buttonsEnabled: Boolean get() = imageModel != null
}
