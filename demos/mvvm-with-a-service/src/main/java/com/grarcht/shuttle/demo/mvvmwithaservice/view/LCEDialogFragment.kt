package com.grarcht.shuttle.demo.mvvmwithaservice.view

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.DialogFragment
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.mvvm.R

private const val ANIMATION_DURATION = 750L
private const val DIALOG_TYPE = "dialog_type"
private const val ERROR_MESSAGE = "error_message"
private const val FADE_OUT_END_ALPHA = 0F
private const val FADE_OUT_START_ALPHA = 1F
private const val HEIGHT_FACTOR = 0.75
private const val IMAGE_DATA = "image_data"
private const val WIDTH_FACTOR = 0.75

/**
 * Used to display the loading, content (retrieved mage), and error views.
 */
class LCEDialogFragment : DialogFragment() {
    private val bitmapDecoder = BitmapDecoder()
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.lce_view, container)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setDialogMetrics(dialog)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadView()
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
        val imageView = view?.findViewById<ImageView>(R.id.retrievedImage)
        imageView?.let { image ->
            image.visibility = View.VISIBLE

            val bitmap = bitmapDecoder.decodeBitmap(imageModel?.imageData as ByteArray)
            bitmap?.let {
                image.setImageBitmap(it)
            }
        }
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
        val loadingLayout = view?.findViewById<FrameLayout>(R.id.loadingLayout)
        fadeOutViewAnimator = ObjectAnimator.ofFloat(
            loadingLayout,
            View.ALPHA,
            FADE_OUT_START_ALPHA,
            FADE_OUT_END_ALPHA
        )
        fadeOutViewAnimator?.let {
            it.duration = ANIMATION_DURATION
            it.addUpdateListener { animation ->
                val animatedValue: Float = animation.animatedValue as? Float ?: FADE_OUT_END_ALPHA
                view?.alpha = FADE_OUT_START_ALPHA - animatedValue

                if (animatedValue == FADE_OUT_END_ALPHA) {
                    loadingLayout?.visibility = View.GONE
                    fadeOutViewAnimator?.removeAllUpdateListeners()
                    if (dismissOnFadeOut) {
                        dismiss()
                    }
                }
            }
            it.start()
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
        val displayMetrics = DisplayMetrics()
        var screenWidth = 0
        var screenHeight = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = dialog.window?.windowManager?.currentWindowMetrics?.bounds
            bounds?.let {
                screenHeight = it.height()
                screenWidth = it.width()
            }
        } else {
            @Suppress("DEPRECATION") dialog.window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            screenHeight = displayMetrics.heightPixels
            screenWidth = displayMetrics.widthPixels
        }

        return Pair(screenWidth, screenHeight)
    }

    private fun extractArguments() {
        arguments?.let {
            val dialogTypeValue = it.getInt(DIALOG_TYPE, DialogType.LOADING.typeValue)
            dialogType = DialogType.toDialogType(dialogTypeValue)

            when (dialogTypeValue) {
                DialogType.LOADING.typeValue -> {
                }

                DialogType.CONTENT.typeValue -> {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                        imageModel = it.getSerializable(IMAGE_DATA, ImageModel::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        imageModel = it.getSerializable(IMAGE_DATA) as ImageModel
                    }
                }

                DialogType.ERROR.typeValue -> {
                    errorMessage = it.getString(ERROR_MESSAGE) as String
                }
            }
        }

    }

    companion object {
        const val TAG_LCE_LOADING = "LCEDialogFragment"
        const val TAG_LCE_CONTENT = "LCEDialogFragment"
        const val TAG_LCE_ERROR = "LCEDialogFragment"

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