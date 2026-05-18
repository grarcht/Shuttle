package com.grarcht.shuttle.demo.core.animation

import android.content.Context
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.util.Log
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import java.io.IOException

private const val ANIMATION_ASPECT_HEIGHT = 9
private const val ANIMATION_ASPECT_WIDTH = 16
private const val ANIMATION_BG_BLUE = 0x14
private const val ANIMATION_BG_GREEN = 0x17
private const val ANIMATION_BG_RED = 0x1A
private const val ANIMATION_CORNER_RADIUS_DP = 12f
private const val ANIMATION_HORIZONTAL_INSET_DP = 32
private const val ANIMATION_SCRIM_ALPHA = 0x8C
private const val ANIMATION_SCRIM_BLUE = 0x29
private const val ANIMATION_SCRIM_GREEN = 0x2D
private const val ANIMATION_SCRIM_RED = 0x32
private const val LOG_PLAYBACK_FAILED = "Animation playback failed"
private const val LOG_TAG = "AnimationOverlayPlayer"

/**
 * Plays a fullscreen animation overlay on top of this [FrameLayout] by rendering a raw video
 * resource through a [android.view.TextureView] backed by a [android.media.MediaPlayer]. The
 * overlay includes a semi-transparent scrim and rounded corners, and is dismissed when playback
 * completes or the user taps the scrim. [onComplete] is invoked when the overlay is removed.
 *
 * Note: [android.view.TextureView] is used instead of [android.view.SurfaceView] because it
 * renders within the [android.view.View] hierarchy, allowing the rounded corner outline clip to
 * work correctly.
 *
 * @param rawResId the raw resource ID of the video file to play.
 * @param onComplete called when the overlay has been dismissed.
 */
fun FrameLayout.playAnimationOverlay(rawResId: Int, onComplete: () -> Unit) {
    val ctx = context
    val cornerRadiusPx = cornerRadiusPx(ctx)
    val (containerWidth, containerHeight) = calculateContainerSize(ctx)
    val scrim = buildScrim(ctx)
    val container = buildContainer(ctx, containerWidth, containerHeight)
    val mediaPlayer = MediaPlayer()
    val textureView = buildTextureView(ctx)

    fun dismiss() {
        mediaPlayer.runCatching {
            stop()
            release()
        }
        removeView(scrim)
        onComplete()
    }

    textureView.surfaceTextureListener = buildSurfaceTextureListener(ctx, rawResId, mediaPlayer, ::dismiss)
    applyRoundedCorners(container, cornerRadiusPx)
    scrim.setOnClickListener { dismiss() }
    container.addView(textureView)
    scrim.addView(container)
    addView(scrim)
}

private fun cornerRadiusPx(ctx: Context): Float =
    ANIMATION_CORNER_RADIUS_DP * ctx.resources.displayMetrics.density

private fun calculateContainerSize(ctx: Context): Pair<Int, Int> {
    val density = ctx.resources.displayMetrics.density
    val screenWidth = ctx.resources.displayMetrics.widthPixels
    val horizontalInsetPx = (ANIMATION_HORIZONTAL_INSET_DP * density).toInt()
    val containerWidth = screenWidth - 2 * horizontalInsetPx
    val containerHeight = containerWidth * ANIMATION_ASPECT_HEIGHT / ANIMATION_ASPECT_WIDTH
    return containerWidth to containerHeight
}

private fun buildScrim(ctx: Context): FrameLayout {
    val scrim = FrameLayout(ctx)
    scrim.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
    scrim.setBackgroundColor(Color.argb(ANIMATION_SCRIM_ALPHA, ANIMATION_SCRIM_RED, ANIMATION_SCRIM_GREEN, ANIMATION_SCRIM_BLUE))
    return scrim
}

private fun buildContainer(ctx: Context, width: Int, height: Int): FrameLayout {
    val container = FrameLayout(ctx)
    container.layoutParams = FrameLayout.LayoutParams(width, height, Gravity.CENTER)
    container.setBackgroundColor(Color.rgb(ANIMATION_BG_RED, ANIMATION_BG_GREEN, ANIMATION_BG_BLUE))
    return container
}

private fun buildTextureView(ctx: Context): TextureView {
    val textureView = TextureView(ctx)
    textureView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
    return textureView
}

private fun buildSurfaceTextureListener(
    ctx: Context,
    rawResId: Int,
    mediaPlayer: MediaPlayer,
    onDismiss: () -> Unit
): TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, w: Int, h: Int) {
        mediaPlayer.startPlayback(ctx, rawResId, surfaceTexture, onDismiss)
    }
    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, w: Int, h: Int) {}
    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean = true
    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
}

private fun MediaPlayer.startPlayback(
    ctx: Context,
    rawResId: Int,
    surfaceTexture: SurfaceTexture,
    onDismiss: () -> Unit
) {
    try {
        setSurface(Surface(surfaceTexture))
        val afd = ctx.resources.openRawResourceFd(rawResId)
        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        setOnCompletionListener { onDismiss() }
        setOnErrorListener { _, _, _ ->
            onDismiss()
            true
        }
        prepareAsync()
        setOnPreparedListener { it.start() }
    } catch (e: IOException) {
        Log.e(LOG_TAG, LOG_PLAYBACK_FAILED, e)
        onDismiss()
    } catch (e: IllegalStateException) {
        Log.e(LOG_TAG, LOG_PLAYBACK_FAILED, e)
        onDismiss()
    }
}

private fun applyRoundedCorners(container: FrameLayout, cornerRadiusPx: Float) {
    // Defer clipToOutline until after layout so the outline has real dimensions
    container.post {
        container.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: android.graphics.Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx)
            }
        }
        container.clipToOutline = true
    }
}
