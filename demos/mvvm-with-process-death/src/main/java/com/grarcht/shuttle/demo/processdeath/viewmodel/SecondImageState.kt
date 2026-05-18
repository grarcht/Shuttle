package com.grarcht.shuttle.demo.processdeath.viewmodel

import com.grarcht.shuttle.demo.core.image.ImageModel

/**
 * Represents the loading state of the image on the second screen in the MVVM with Process Death
 * demo. The state machine transitions from [Loading] to [Success], [LostToProcessDeath], or
 * [Error] depending on whether Shuttle cargo pickup succeeds or the cargo was not preserved.
 */
sealed class SecondImageState {
    /** The image is being retrieved from the Shuttle warehouse or in-memory cache. */
    object Loading : SecondImageState()

    /**
     * The image was retrieved successfully.
     *
     * @property imageModel the retrieved image data.
     */
    data class Success(val imageModel: ImageModel) : SecondImageState()

    /**
     * The cargo ID was empty, meaning the process was killed and the data was not preserved by
     * Shuttle. This demonstrates the data-loss scenario that Shuttle is designed to prevent.
     */
    object LostToProcessDeath : SecondImageState()

    /**
     * An error occurred while retrieving the image.
     *
     * @property message a description of the error.
     */
    data class Error(val message: String) : SecondImageState()
}
