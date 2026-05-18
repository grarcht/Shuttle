package com.grarcht.shuttle.demo.core.view

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.grarcht.shuttle.demo.core.R

private const val ANIMATION_DURATION = 750L
const val BYTES_PER_KB = 1024.0
private const val FADE_IN_START_ALPHA = 0F
private const val FADE_OUT_END_ALPHA = 0F
private const val FADE_OUT_START_ALPHA = 1F

// Must stay in sync with HORIZONTAL_BIAS in DemoSecondScreenLayout (demos/core/compose)
private const val HORIZONTAL_BIAS = 0.4f

/**
 * Fades out the loading layout found inside [view] and simultaneously fades in [viewToFadeIn].
 * Returns the [ObjectAnimator] driving the transition, or null if the loading layout cannot
 * be found.
 *
 * @param view the root view that contains the loading layout by id.
 * @param viewToFadeIn the view to reveal as the loading layout fades out.
 */
fun hideLoadingView(view: View?, viewToFadeIn: View?): ObjectAnimator? {
    val loadingLayout = view?.findViewById<FrameLayout>(R.id.loadingLayout) ?: return null
    val animator = ObjectAnimator.ofFloat(loadingLayout, View.ALPHA, FADE_OUT_START_ALPHA, FADE_OUT_END_ALPHA)
    animator.duration = ANIMATION_DURATION
    animator.addUpdateListener { animation ->
        val animatedValue: Float = animation.animatedValue as? Float ?: FADE_OUT_END_ALPHA
        viewToFadeIn?.alpha = FADE_OUT_START_ALPHA - animatedValue
        if (animatedValue == FADE_OUT_END_ALPHA) {
            loadingLayout.visibility = View.GONE
            animator.removeAllUpdateListeners()
        }
    }
    animator.start()
    return animator
}

/**
 * Makes the error layout inside [view] visible and fades in while fading out the loading layout.
 * Returns the [ObjectAnimator] driving the transition, or null if the loading layout cannot
 * be found.
 *
 * @param view the root view that contains the error and loading layouts by id.
 */
fun showErrorView(view: View?): ObjectAnimator? {
    val errorLayout = view?.findViewById<FrameLayout>(R.id.errorLayout)
    errorLayout?.apply {
        alpha = FADE_IN_START_ALPHA
        visibility = View.VISIBLE
    }
    return hideLoadingView(view, errorLayout)
}

/**
 * Scales [bitmap] to fill [imageView] and applies a horizontally biased crop using a custom
 * [android.graphics.Matrix] so the subject remains visible when the image is wider than the view.
 *
 * @param imageView the view to display the bitmap in.
 * @param bitmap the bitmap to scale and position.
 */
fun applyBiasedCrop(imageView: ImageView, bitmap: Bitmap) {
    imageView.scaleType = ImageView.ScaleType.MATRIX
    imageView.setImageBitmap(bitmap)
    imageView.post {
        val vw = imageView.width.toFloat()
        val vh = imageView.height.toFloat()
        if (vw == 0f || vh == 0f) return@post
        val scale = maxOf(vw / bitmap.width, vh / bitmap.height)
        val dx = -(bitmap.width * scale - vw) / 2f * (1f + HORIZONTAL_BIAS)
        val dy = -(bitmap.height * scale - vh) / 2f
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        imageView.imageMatrix = matrix
    }
}

/**
 * Adds the navigation bar inset to the bottom padding of the card overlay inside [parent] so
 * it remains visible above the gesture bar or navigation buttons.
 *
 * @param parent the view that contains the card overlay layout by id.
 */
fun applyNavBarInsetToCard(parent: View) {
    val card = parent.findViewById<LinearLayout>(R.id.cardOverlay) ?: return
    val basePaddingV = parent.resources.getDimensionPixelSize(R.dimen.second_screen_card_padding_v)
    ViewCompat.setOnApplyWindowInsetsListener(card) { v, insets ->
        val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        v.updatePadding(bottom = basePaddingV + navBar.bottom)
        insets
    }
}

/**
 * Sets the image-size label inside [parent] to a human-readable representation of [bytes],
 * formatted as bytes, kilobytes, or megabytes depending on the magnitude.
 *
 * @param parent the view that contains the image size [android.widget.TextView] by id.
 * @param bytes the image size in bytes.
 */
fun setImageSizeText(parent: View, bytes: Long) {
    val kb = bytes / BYTES_PER_KB
    val mb = kb / BYTES_PER_KB
    val text = when {
        mb >= 1.0 -> parent.context.getString(R.string.second_screen_image_size_mb, mb.toFloat())
        kb >= 1.0 -> parent.context.getString(R.string.second_screen_image_size_kb, kb.toFloat())
        else -> parent.context.getString(R.string.second_screen_image_size_b, bytes)
    }
    parent.findViewById<TextView>(R.id.imageSizeText)?.text = text
}
