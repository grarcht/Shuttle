package com.grarcht.shuttle.demo.mvvmwithaservice.view

import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.view.setImageSizeText
import com.grarcht.shuttle.demo.mvvmwithaservice.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ANIMATION_DURATION = 750L
private const val DIALOG_TYPE = "dialog_type"
private const val ERROR_MESSAGE = "error_message"
private const val FADE_OUT_END_ALPHA = 0F
private const val FADE_OUT_START_ALPHA = 1F
private const val IMAGE_DATA = "image_data"

/**
 * Used to display the loading, content (retrieved mage), and error views.
 */
class LCEDialogFragment : DialogFragment() {
    private var contentLoadingProgressBar: ContentLoadingProgressBar? = null
    private var errorMessage: String = ""
    private var fadeOutViewAnimator: ObjectAnimator? = null
    private var imageModel: ImageModel? = null

    var dialogType: DialogType = DialogType.LOADING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
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
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (dialogType) {
            DialogType.LOADING -> initLoadingView()
            DialogType.CONTENT -> showSuccessView()
            DialogType.ERROR -> showErrorView()
        }
    }

    override fun onDestroyView() {
        fadeOutViewAnimator?.cancel()
        super.onDestroyView()
    }

    private fun initLoadingView() {
        view?.let {
            contentLoadingProgressBar = it.findViewById(R.id.loading_indicator)
            contentLoadingProgressBar?.show()
        }
    }

    private fun showSuccessView() {
        showSuccessLayout()
        loadAndDisplayImage(view?.findViewById(R.id.retrievedImage), imageModel?.imageData)
        view?.let { setImageSizeText(it, imageModel?.imageData?.size?.toLong() ?: 0L) }
        val card = view?.findViewById<LinearLayout>(R.id.cardOverlay) ?: return
        applyNavBarInset(card)
    }

    private fun showSuccessLayout() {
        view?.findViewById<FrameLayout>(R.id.loadingLayout)?.visibility = View.GONE
        view?.findViewById<FrameLayout>(R.id.successLayout)?.visibility = View.VISIBLE
    }

    private fun loadAndDisplayImage(imageView: ImageView?, imageData: ByteArray?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) { imageData?.let { BitmapDecoder.decodeBitmap(it) } }
            imageView?.setImageBitmap(bitmap)
        }
    }

    private fun applyNavBarInset(card: LinearLayout) {
        val basePaddingV = resources.getDimensionPixelSize(com.grarcht.shuttle.demo.core.R.dimen.second_screen_card_padding_v)
        ViewCompat.setOnApplyWindowInsetsListener(card) { v, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = basePaddingV + navBar.bottom)
            insets
        }
    }

    private fun showErrorView() {
        showErrorLayout()
        val card = view?.findViewById<LinearLayout>(R.id.errorCardOverlay) ?: return
        applyNavBarInset(card)
    }

    private fun showErrorLayout() {
        view?.apply {
            findViewById<FrameLayout>(R.id.loadingLayout)?.visibility = View.GONE
            findViewById<FrameLayout>(R.id.errorLayout)?.visibility = View.VISIBLE
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

    private fun extractArguments() {
        arguments?.let {
            val dialogTypeValue = it.getInt(DIALOG_TYPE, DialogType.LOADING.typeValue)
            dialogType = DialogType.toDialogType(dialogTypeValue)
            when (dialogTypeValue) {
                DialogType.CONTENT.typeValue -> imageModel = extractImageModel(it)
                DialogType.ERROR.typeValue -> errorMessage = it.getString(ERROR_MESSAGE) ?: ""
            }
        }
    }

    private fun extractImageModel(args: Bundle): ImageModel? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            args.getSerializable(IMAGE_DATA, ImageModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            args.getSerializable(IMAGE_DATA) as? ImageModel
        }

    companion object {
        const val TAG_LCE_LOADING = "LCEDialogFragment_Loading"
        const val TAG_LCE_CONTENT = "LCEDialogFragment_Content"
        const val TAG_LCE_ERROR = "LCEDialogFragment_Error"

        fun createLoadingDialogWith(): LCEDialogFragment =
            createFragmentWith { it.putInt(DIALOG_TYPE, DialogType.LOADING.typeValue) }

        fun createContentDialogWith(imageModel: ImageModel): LCEDialogFragment =
            createFragmentWith {
                it.putInt(DIALOG_TYPE, DialogType.CONTENT.typeValue)
                it.putSerializable(IMAGE_DATA, imageModel)
            }

        fun createErrorDialogWith(errorMessage: String): LCEDialogFragment =
            createFragmentWith {
                it.putInt(DIALOG_TYPE, DialogType.ERROR.typeValue)
                it.putString(ERROR_MESSAGE, errorMessage)
            }

        private fun createFragmentWith(configure: (Bundle) -> Unit): LCEDialogFragment {
            val fragment = LCEDialogFragment()
            val args = Bundle()
            configure(args)
            fragment.arguments = args
            return fragment
        }
    }
}
