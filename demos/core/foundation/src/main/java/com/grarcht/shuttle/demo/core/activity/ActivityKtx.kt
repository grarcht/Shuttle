package com.grarcht.shuttle.demo.core.activity

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

/**
 * Configures the activity window for edge-to-edge display with transparent status and navigation
 * bars. On Android Q and above, contrast enforcement is also disabled so the bars remain fully
 * transparent rather than adopting a system-provided scrim.
 */
fun ComponentActivity.setupEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = false
    }
}

/**
 * Removes the clip-to-padding and clip-children constraints from the window content view and its
 * parent so that child views can draw beyond the standard content boundaries, which is required
 * when rendering floating overlays over the entire screen.
 */
fun ComponentActivity.disableWindowContentClipping() {
    val contentView = window.decorView.findViewById<View>(android.R.id.content)
    (contentView?.parent as? ViewGroup)?.let {
        it.clipChildren = false
        it.clipToPadding = false
    }
    (contentView as? ViewGroup)?.let {
        it.clipChildren = false
        it.clipToPadding = false
    }
}
