package com.grarcht.shuttle.demo.mviwithcompose.intent

import android.content.res.Resources
import androidx.annotation.RawRes
import com.grarcht.shuttle.demo.core.image.ImageModel

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

    /**
     * Requests navigation to the second view by transporting the image model safely
     * via Shuttle to avoid a [android.os.TransactionTooLargeException].
     *
     * @property imageModel the image model to transport as cargo.
     */
    data class NavigateWithShuttle(val imageModel: ImageModel?) : CargoTransportIntent()

    /**
     * Requests navigation to the second view by passing the image model directly
     * via [android.content.Intent], demonstrating the crash scenario.
     *
     * @property imageModel the image model to pass directly in the intent.
     */
    data class NavigateNormally(val imageModel: ImageModel?) : CargoTransportIntent()

    /**
     * Requests that all cargo delivered by Shuttle be cleaned up from the warehouse.
     */
    data object CleanUp : CargoTransportIntent()
}
