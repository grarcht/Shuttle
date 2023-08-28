package com.grarcht.shuttle.demo.mvc.controller

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.os.getParcelableWith
import com.grarcht.shuttle.demo.mvc.R
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

private const val ANIMATION_DURATION = 750L
private const val FADE_OUT_END_ALPHA = 0F
private const val FADE_OUT_START_ALPHA = 1F
private const val FADE_IN_START_ALPHA = 0F
private const val LOG_TAG = "MVCSecondControllerFragment"

@AndroidEntryPoint
class MVCSecondControllerFragment : Fragment() {
    private val bitmapDecoder = BitmapDecoder()
    private lateinit var contentLoadingProgressBar: ContentLoadingProgressBar
    private var hideLoadingViewAnimator: ObjectAnimator? = null
    var imageModel: ImageModel? = null
    var storedCargoId: String? = null

    @Inject
    lateinit var shuttle: Shuttle
    private var deferredImageLoad: Deferred<Unit>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.second_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        extractArgsFrom(savedInstanceState, arguments)
        initLoadingView(view)
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
        lifecycleScope.launch {
            getShuttleChannel()
                .consumeAsFlow()
                .collectLatest { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttlePickupCargoResult.Loading -> {
                            view?.let { initLoadingView(it) }
                        }

                        is ShuttlePickupCargoResult.Success<*> -> {
                            showSuccessView(view, shuttleResult.data as ImageModel)
                            cancel()
                        }

                        is ShuttlePickupCargoResult.Error<*> -> {
                            showErrorView(view)
                            cancel()
                        }

                        else -> {
                            // ignore
                        }
                    }
                }
        }
    }

    private suspend fun getShuttleChannel(): Channel<ShuttlePickupCargoResult> {
        val cargoId = storedCargoId ?: ImageMessageType.ImageData.value
        return shuttle.pickupCargo<Serializable>(cargoId = cargoId)
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
