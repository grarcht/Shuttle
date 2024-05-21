package com.grarcht.shuttle.demo.mvvmwithnavigation.view

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.os.getParcelableWith
import com.grarcht.shuttle.demo.mvvmwithnavigation.R
import com.grarcht.shuttle.demo.mvvmwithnavigation.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ANIMATION_DURATION = 750L
private const val FADE_OUT_END_ALPHA = 0F
private const val FADE_OUT_START_ALPHA = 1F
private const val FADE_IN_START_ALPHA = 0F
private const val LOG_TAG = "MVVMNavSecondViewFragment"

@AndroidEntryPoint
class MVVMNavSecondViewFragment : Fragment() {
    private val bitmapDecoder = BitmapDecoder()
    private lateinit var contentLoadingProgressBar: ContentLoadingProgressBar
    private var deferredImageLoad: Deferred<Unit>? = null
    private var hideLoadingViewAnimator: ObjectAnimator? = null
    private var imageModel: ImageModel? = null
    private var storedCargoId: String? = null
    private val viewModel by viewModels<SecondViewModel>()

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.second_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        extractArgsFrom(savedInstanceState, arguments)
        loadImageModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Saving the state in a guarded fashion.  The super call must have the shuttle bundle to ensure apps do not
        // crash during transactions with saving and restoring states.
        val outStateShuttleBundle = shuttle.bundleCargoWith(outState)
            .logTag(LOG_TAG)
            .transport(ImageMessageType.ImageData.value, imageModel)
            .create()
        super.onSaveInstanceState(outStateShuttleBundle)
    }

    override fun onDestroyView() {
        deferredImageLoad?.cancel()
        contentLoadingProgressBar.hide()
        super.onDestroyView()
    }

    private fun extractArgsFrom(savedInstanceState: Bundle?, arguments: Bundle?) {
        val bundle: Bundle? = savedInstanceState ?: arguments
        bundle?.let {
            val cargo: ShuttleParcelCargo? =
                it.getParcelableWith(ImageMessageType.ImageData.value, ShuttleParcelCargo::class.java)
            storedCargoId = cargo?.cargoId
        }
    }

    private fun loadImageModel() {
        if (null != imageModel) {
            showSuccessView(view, imageModel as ImageModel)
            return
        }

        val cargoId = storedCargoId ?: ""
        if (cargoId.isEmpty()) {
            showErrorView(view)
            return
        }

        lifecycleScope.launch {
            viewModel
                .loadImage(shuttle, cargoId)
                .collectLatest { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttlePickupCargoResult.Loading -> {
                            view?.let { initLoadingView(it) }
                        }

                        is ShuttlePickupCargoResult.Success<*> -> {
                            imageModel = shuttleResult.data as ImageModel
                            view?.let { showSuccessView(view, imageModel as ImageModel) }
                            cancel()
                        }

                        is ShuttlePickupCargoResult.Error<*> -> {
                            view?.let { showErrorView(it) }
                            cancel()
                        }

                        else -> {
                            // ignore
                        }
                    }
                }
        }
    }

    private fun initLoadingView(view: View) {
        contentLoadingProgressBar = view.findViewById(R.id.loading_indicator)
        contentLoadingProgressBar.show()
    }

    private fun hideLoadingView(view: View?, viewToFadeIn: View?) {
        val loadingLayout = view?.findViewById<FrameLayout>(R.id.loadingLayout)
        hideLoadingViewAnimator = ObjectAnimator.ofFloat(
            loadingLayout,
            View.ALPHA,
            FADE_OUT_START_ALPHA,
            FADE_OUT_END_ALPHA
        )
        hideLoadingViewAnimator?.duration = ANIMATION_DURATION
        hideLoadingViewAnimator?.addUpdateListener { animation ->
            val animatedValue: Float = animation.animatedValue as? Float ?: FADE_OUT_END_ALPHA
            viewToFadeIn?.alpha = FADE_OUT_START_ALPHA - animatedValue

            if (animatedValue == FADE_OUT_END_ALPHA) {
                loadingLayout?.visibility = View.GONE
                hideLoadingViewAnimator?.removeAllUpdateListeners()
            }
        }
        hideLoadingViewAnimator?.start()
    }

    private fun showErrorView(view: View?) {
        val errorLayout = view?.findViewById<FrameLayout>(R.id.errorLayout)
        errorLayout?.apply {
            alpha = FADE_IN_START_ALPHA
            visibility = View.VISIBLE
        }
        hideLoadingView(view, errorLayout)
    }

    private fun showSuccessView(view: View?, imageModel: ImageModel) {
        this.imageModel = imageModel

        val imageView = view?.findViewById<ImageView>(R.id.retrievedImage)
        imageView?.let { image ->
            image.alpha = FADE_IN_START_ALPHA
            image.visibility = View.VISIBLE

            val bitmap = bitmapDecoder.decodeBitmap(imageModel.imageData)
            bitmap?.let {
                image.setImageBitmap(it)
                hideLoadingView(view, imageView)
            }
        }
    }
}
