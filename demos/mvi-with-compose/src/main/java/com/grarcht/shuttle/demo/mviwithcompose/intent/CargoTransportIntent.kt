package com.grarcht.shuttle.demo.mviwithcompose.intent

import android.content.res.Resources
import androidx.annotation.RawRes

/**
 * Represents the set of user or system actions that can be dispatched to trigger
 * cargo transport operations in the first view. Each subtype corresponds to a
 * distinct operation that the view model can act on.
 */
sealed class CargoTransportIntent {
    /**
     * Requests that an image be loaded from a raw resource and prepared for transport
     * as cargo to the second view.
     *
     * @property resources the app resources used to read the raw image data.
     * @property imageId the raw resource ID of the image to load.
     */
    data class LoadImage(val resources: Resources, @RawRes val imageId: Int) : CargoTransportIntent()
}
