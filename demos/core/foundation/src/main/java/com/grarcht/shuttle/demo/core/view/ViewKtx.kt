package com.grarcht.shuttle.demo.core.view

import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.applySystemBarTopInset(@IdRes contentLayoutId: Int) {
    val contentLayout = findViewById<View>(contentLayoutId)
    val originalTopPadding = contentLayout.paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        contentLayout.setPadding(
            contentLayout.paddingLeft,
            originalTopPadding + systemBars.top,
            contentLayout.paddingRight,
            contentLayout.paddingBottom
        )
        WindowInsetsCompat.CONSUMED
    }
}
