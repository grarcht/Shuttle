package com.grarcht.shuttle.demo.mvvmwithaservice.view

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.mvvmwithaservice.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ANIMATION_DURATION = 750L
private const val BYTES_PER_KB = 1024.0
private const val DIALOG_TYPE = "dialog_type"
private const val ERROR_MESSAGE = "error_message"
private const val FADE_OUT_END_ALPHA = 0F
private const val FADE_OUT_START_ALPHA = 1F
private const val HEIGHT_FACTOR = 0.75
private const val IMAGE_DATA = "image_data"
private const val SIZE_THRESHOLD = 1.0
private const val WIDTH_FACTOR = 0.75

/**
 * Used to display the loading, content (retrieved mage), and error views.
 */
class LCEDialogFragment : DialogFragment() {
    private var contentLoadingProgressBar: ContentLoadingProgressBar? = null
    private var errorMessage: String = ""
    private var fadeOutViewAnimator: ObjectAnimator? = null
    private var imageModel: ImageModel? = null
    private var listener: OnDismissListener? = null

    var dialogType: DialogType = DialogType.LOADING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
        extractArguments()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.lce_view, container)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setDialogMetrics(dialog)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadView()
    }

    override fun onDestroyView() {
        fadeOutViewAnimator?.cancel()
        super.onDestroyView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismiss(dialog)
    }

    fun setOnDismissListener(listener: OnDismissListener?) {
        this.listener = listener
    }

    private fun loadView() {
        when (dialogType) {
            DialogType.LOADING -> initLoadingView()
            DialogType.CONTENT -> showSuccessView()
            DialogType.ERROR -> showErrorView()
        }
    }

    private fun initLoadingView() {
        view?.let {
            contentLoadingProgressBar = it.findViewById(R.id.loading_indicator)
            contentLoadingProgressBar?.show()
        }
    }

    private fun showSuccessView() {
        val successLayout = view?.findViewById<FrameLayout>(R.id.successLayout) ?: return
        successLayout.visibility = View.VISIBLE

        val imageData = imageModel?.imageData
        val imageView = view?.findViewById<ImageView>(R.id.retrievedImage)
        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) { imageData?.let { BitmapDecoder.decodeBitmap(it) } }
            imageView?.setImageBitmap(bitmap)
        }

        val bytes = imageModel?.imageData?.size?.toLong() ?: 0L
        val kb = bytes / BYTES_PER_KB
        val mb = kb / BYTES_PER_KB
        val sizeText = when {
            mb >= SIZE_THRESHOLD -> getString(com.grarcht.shuttle.demo.core.R.string.second_screen_image_size_mb, mb.toFloat())
            kb >= SIZE_THRESHOLD -> getString(com.grarcht.shuttle.demo.core.R.string.second_screen_image_size_kb, kb.toFloat())
            else -> getString(com.grarcht.shuttle.demo.core.R.string.second_screen_image_size_b, bytes)
        }
        view?.findViewById<TextView>(R.id.imageSizeText)?.text = sizeText
    }

    private fun showErrorView() {
        view?.let {
            val errorLayout = it.findViewById<FrameLayout>(R.id.errorLayout)
            errorLayout?.apply {
                visibility = View.VISIBLE
            }
        }
    }

    fun fadeOutView(dismissOnFadeOut: Boolean) {
        val loadingLayout = view?.findViewById<FrameLayout>(R.id.loadingLayout) ?: return
        fadeOutViewAnimator = buildFadeAnimator(loadingLayout)
        fadeOutViewAnimator?.let {
            it.addUpdateListener { animation ->
                val animatedValue: Float = animation.animatedValue as? Float ?: FADE_OUT_END_ALPHA
                onFadeUpdate(animatedValue, loadingLayout, dismissOnFadeOut)
            }
            it.start()
        }
    }

    private fun buildFadeAnimator(loadingLayout: FrameLayout): ObjectAnimator =
        ObjectAnimator.ofFloat(loadingLayout, View.ALPHA, FADE_OUT_START_ALPHA, FADE_OUT_END_ALPHA)
            .also { it.duration = ANIMATION_DURATION }

    private fun onFadeUpdate(animatedValue: Float, loadingLayout: FrameLayout, dismissOnFadeOut: Boolean) {
        view?.alpha = FADE_OUT_START_ALPHA - animatedValue
        if (animatedValue == FADE_OUT_END_ALPHA) {
            loadingLayout.visibility = View.GONE
            fadeOutViewAnimator?.removeAllUpdateListeners()
            if (dismissOnFadeOut) dismiss()
        }
    }

    private fun setDialogMetrics(dialog: Dialog) {
        val dimensions = getWindowDimensions(dialog)
        val width: Int = (dimensions.first * WIDTH_FACTOR).toInt()
        val height: Int = (dimensions.second * HEIGHT_FACTOR).toInt()
        dialog.window?.let {
            it.setLayout(width, height)
            it.setGravity(Gravity.CENTER)
        }
    }

    private fun getWindowDimensions(dialog: Dialog): Pair<Int, Int> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = dialog.window?.windowManager?.currentWindowMetrics?.bounds
            if (bounds != null) {
                return Pair(bounds.width(), bounds.height())
            }
        }
        val displayMetrics = dialog.context.resources.displayMetrics
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    private fun extractArguments() {
        arguments?.let {
            val dialogTypeValue = it.getInt(DIALOG_TYPE, DialogType.LOADING.typeValue)
            dialogType = DialogType.toDialogType(dialogTypeValue)

            when (dialogTypeValue) {
                DialogType.LOADING.typeValue -> {
                }

                DialogType.CONTENT.typeValue -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        imageModel = it.getSerializable(IMAGE_DATA, ImageModel::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        imageModel = it.getSerializable(IMAGE_DATA) as? ImageModel
                    }
                }

                DialogType.ERROR.typeValue -> {
                    errorMessage = it.getString(ERROR_MESSAGE) ?: ""
                }
            }
        }
    }

    companion object {
        const val TAG_LCE_LOADING = "LCEDialogFragment_Loading"
        const val TAG_LCE_CONTENT = "LCEDialogFragment_Content"
        const val TAG_LCE_ERROR = "LCEDialogFragment_Error"

        /**
         * A factory function for creating a loading dialog.
         */
        fun createLoadingDialogWith(): LCEDialogFragment {
            val fragment = LCEDialogFragment()
            val args = Bundle()
            args.putInt(DIALOG_TYPE, DialogType.LOADING.typeValue)
            fragment.arguments = args
            return fragment
        }

        /**
         * A factory function for creating a content dialog to show the retrieved image cargo.
         */
        fun createContentDialogWith(imageModel: ImageModel): LCEDialogFragment {
            val fragment = LCEDialogFragment()
            val args = Bundle()
            args.putInt(DIALOG_TYPE, DialogType.CONTENT.typeValue)
            args.putSerializable(IMAGE_DATA, imageModel)
            fragment.arguments = args
            return fragment
        }

        /**
         * A factory function for creating an error dialog.
         */
        fun createErrorDialogWith(errorMessage: String): LCEDialogFragment {
            val fragment = LCEDialogFragment()
            val args = Bundle()
            args.putInt(DIALOG_TYPE, DialogType.ERROR.typeValue)
            args.putString(ERROR_MESSAGE, errorMessage)
            fragment.arguments = args
            return fragment
        }
    }
}
