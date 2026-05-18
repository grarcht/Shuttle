package com.grarcht.shuttle.demo.core.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.withClip
import com.grarcht.shuttle.demo.core.R

private const val ARC_ANGLE_HALF = 180f
private const val ARC_ANGLE_QUARTER = 90f

/**
 * A custom [android.widget.FrameLayout] that draws a rounded-rectangle card with an arc-shaped
 * cutout at the top edge. Child views are clipped to the card shape. A software rendering layer
 * is required because non-convex path clipping is unsupported by the GPU renderer on older APIs.
 */
class CardWithCutoutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
    private val cutoutRadius = resources.getDimension(R.dimen.cutout_radius)
    private val cutoutCenterXFromRight = resources.getDimension(R.dimen.cutout_center_x_from_right)
    private val strokeWidth = resources.getDimension(R.dimen.card_image_inset)

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@CardWithCutoutView.strokeWidth
    }

    private val cardPath = Path()
    private val strokePath = Path()

    init {
        setWillNotDraw(false)
        // Non-convex path clipping (the card cutout) is unsupported by the GPU renderer on older APIs.
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    /**
     * Sets the fill and stroke color of the card and invalidates the view so it redraws
     * with the new color.
     *
     * @param color the color to apply, as a packed [androidx.annotation.ColorInt] value.
     */
    fun setCardColor(@ColorInt color: Int) {
        backgroundPaint.color = color
        strokePaint.color = color
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildPaths(w.toFloat(), h.toFloat())
    }

    private fun buildPaths(w: Float, h: Float) {
        val r = cornerRadius
        val cr = cutoutRadius
        val cx = w - cutoutCenterXFromRight

        cardPath.reset()
        cardPath.moveTo(r, 0f)
        cardPath.lineTo(cx - cr, 0f)
        cardPath.arcTo(RectF(cx - cr, -cr, cx + cr, cr), ARC_ANGLE_HALF, -ARC_ANGLE_HALF, false)
        cardPath.lineTo(w - r, 0f)
        cardPath.arcTo(RectF(w - 2 * r, 0f, w, 2 * r), -ARC_ANGLE_QUARTER, ARC_ANGLE_QUARTER, false)
        cardPath.lineTo(w, h - r)
        cardPath.arcTo(RectF(w - 2 * r, h - 2 * r, w, h), 0f, ARC_ANGLE_QUARTER, false)
        cardPath.lineTo(r, h)
        cardPath.arcTo(RectF(0f, h - 2 * r, 2 * r, h), ARC_ANGLE_QUARTER, ARC_ANGLE_QUARTER, false)
        cardPath.lineTo(0f, r)
        cardPath.arcTo(RectF(0f, 0f, 2 * r, 2 * r), ARC_ANGLE_HALF, ARC_ANGLE_QUARTER, false)
        cardPath.close()

        strokePath.reset()
        strokePath.arcTo(RectF(cx - cr, -cr, cx + cr, cr), ARC_ANGLE_HALF, -ARC_ANGLE_HALF, false)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(cardPath, backgroundPaint)
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withClip(cardPath) {
            super.dispatchDraw(this)
        }
        canvas.drawPath(strokePath, strokePaint)
    }
}
