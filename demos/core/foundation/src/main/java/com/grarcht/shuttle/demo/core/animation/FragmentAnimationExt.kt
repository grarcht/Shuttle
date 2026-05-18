package com.grarcht.shuttle.demo.core.animation

import android.widget.FrameLayout
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment

/**
 * Plays a fullscreen animation overlay on the root [android.widget.FrameLayout] of this
 * [Fragment]'s view hierarchy. Delegates to the [FrameLayout.playAnimationOverlay] extension.
 *
 * @param rawResId the raw resource ID of the video file to play.
 */
fun Fragment.playAnimationOverlay(@RawRes rawResId: Int) {
    (view as? FrameLayout)?.playAnimationOverlay(rawResId) {}
}
