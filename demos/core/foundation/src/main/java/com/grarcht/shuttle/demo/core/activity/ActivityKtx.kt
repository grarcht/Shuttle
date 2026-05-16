package com.grarcht.shuttle.demo.core.activity

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

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
