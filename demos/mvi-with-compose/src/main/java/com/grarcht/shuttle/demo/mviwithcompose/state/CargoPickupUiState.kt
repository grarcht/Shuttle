package com.grarcht.shuttle.demo.mviwithcompose.state

import com.grarcht.shuttle.demo.core.image.ImageModel

/**
 * Holds the UI state for the second view during cargo pickup. The state reflects
 * whether the cargo retrieval is in progress, has completed successfully with an
 * image model, or has failed with an error.
 *
 * @property isLoading true while cargo retrieval is in progress.
 * @property imageModel the retrieved image data, or null if pickup has not completed.
 * @property error the throwable captured when pickup fails, or null on success.
 */
data class CargoPickupUiState(
    val isLoading: Boolean = true,
    val imageModel: ImageModel? = null,
    val error: Throwable? = null
)
