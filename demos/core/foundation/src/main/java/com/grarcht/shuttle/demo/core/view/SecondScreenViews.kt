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
private const val HORIZONTAL_BIAS = 0.4f

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

fun showErrorView(view: View?): ObjectAnimator? {
    val errorLayout = view?.findViewById<FrameLayout>(R.id.errorLayout)
    errorLayout?.apply {
        alpha = FADE_IN_START_ALPHA
        visibility = View.VISIBLE
    }
    return hideLoadingView(view, errorLayout)
}

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

fun applyNavBarInsetToCard(parent: View) {
    val card = parent.findViewById<LinearLayout>(R.id.cardOverlay) ?: return
    val basePaddingV = parent.resources.getDimensionPixelSize(R.dimen.second_screen_card_padding_v)
    ViewCompat.setOnApplyWindowInsetsListener(card) { v, insets ->
        val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        v.updatePadding(bottom = basePaddingV + navBar.bottom)
        insets
    }
}

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
