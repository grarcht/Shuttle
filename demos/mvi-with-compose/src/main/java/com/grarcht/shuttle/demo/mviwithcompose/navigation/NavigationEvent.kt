package com.grarcht.shuttle.demo.mviwithcompose.navigation

import com.grarcht.shuttle.demo.core.image.ImageModel

/**
 * Represents one-shot navigation side effects emitted by the first view model.
 * The view collects these events and executes the corresponding platform navigation.
 */
sealed class NavigationEvent {
    /**
     * Signals that the view should navigate to the second screen by transporting
     * the image model safely via Shuttle.
     *
     * @property imageModel the image model to transport as cargo.
     */
    data class NavigateWithShuttle(val imageModel: ImageModel?) : NavigationEvent()

    /**
     * Signals that the view should navigate to the second screen by passing the
     * image model directly via [android.content.Intent], demonstrating the crash scenario.
     *
     * @property imageModel the image model to pass directly in the intent.
     */
    data class NavigateNormally(val imageModel: ImageModel?) : NavigationEvent()
}
