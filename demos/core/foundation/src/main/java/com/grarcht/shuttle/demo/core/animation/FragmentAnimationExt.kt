package com.grarcht.shuttle.demo.core.animation

import android.widget.FrameLayout
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment

fun Fragment.playAnimationOverlay(@RawRes rawResId: Int) {
    (view as? FrameLayout)?.playAnimationOverlay(rawResId) {}
}
