package com.grarcht.shuttle.demo.core.compose.ui

import android.net.Uri
import android.widget.VideoView
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private val ANIMATION_CORNER_RADIUS = 12.dp // matches card image shape
private val ANIMATION_HORIZONTAL_PADDING = 32.dp // 16dp card margin + 16dp inset
private const val ANIMATION_ASPECT_RATIO = 16f / 9f
private val ANIMATION_BG_COLOR = Color(0xFF1A1714)
private val ANIMATION_SCRIM_COLOR = Color(0x8C322D29)

/**
 * Full-screen scrim with a centered 16:9 animation overlay. Tapping the scrim dismisses early.
 * The video auto-dismisses by calling [onComplete] when playback finishes.
 */
@Composable
fun DemoNavAnimation(
    @RawRes animationRes: Int,
    onComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ANIMATION_SCRIM_COLOR)
            .clickable(onClick = onComplete),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ANIMATION_HORIZONTAL_PADDING)
                .aspectRatio(ANIMATION_ASPECT_RATIO)
                .clip(RoundedCornerShape(ANIMATION_CORNER_RADIUS))
                .background(ANIMATION_BG_COLOR)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val uri = Uri.parse("android.resource://${ctx.packageName}/$animationRes")
                        setVideoURI(uri)
                        setOnCompletionListener { onComplete() }
                        setOnPreparedListener { it.start() }
                        start()
                    }
                }
            )
        }
    }
}
