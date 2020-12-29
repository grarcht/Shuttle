package com.grarcht.shuttle.demo.mvvm.view

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.mvvm.BR
import com.grarcht.shuttle.demo.mvvm.R
import com.grarcht.shuttle.demo.mvvm.databinding.SecondFragmentBinding
import com.grarcht.shuttle.demo.mvvm.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Deferred
import javax.inject.Inject

private const val ANIMATION_DURATION = 750L
private const val FADE_OUT_END_ALPHA = 0F
private const val FADE_OUT_START_ALPHA = 1F
private const val FADE_IN_START_ALPHA = 0F
private const val LOG_TAG = "MVVMSecondViewFragment"

@AndroidEntryPoint
class MVVMSecondViewFragment : Fragment() {

    private val bitmapDecoder = BitmapDecoder()
    private lateinit var contentLoadingProgressBar: ContentLoadingProgressBar
    private var deferredImageLoad: Deferred<Unit>? = null
    private var hideLoadingViewAnimator: ObjectAnimator? = null
    private val viewModel by viewModels<SecondViewModel>()
    private var imageModel: ImageModel? = null
    private var onPropertyChangeCallback: Observable.OnPropertyChangedCallback? = null
    private var storedCargoId: String? = null

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.second_fragment, container, false)
        DataBindingUtil.bind<SecondFragmentBinding>(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        extractArgsFrom(savedInstanceState, arguments)
        observeBindingNotifications()
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
        viewModel.removeOnPropertyChangedCallback(onPropertyChangeCallback as Observable.OnPropertyChangedCallback)
        super.onDestroyView()
    }

    private fun observeBindingNotifications() {
        onPropertyChangeCallback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                when (propertyId) {
                    BR.shuttlePickupCargoResult -> {
                        when (viewModel.shuttlePickupCargoResult) {
                            ShuttlePickupCargoResult.Loading -> {
                                view?.let { initLoadingView(it) }
                            }
                            is ShuttlePickupCargoResult.Success<*> -> {
                                if (null != view && viewModel.imageModel != null) {
                                    showSuccessView(view, viewModel.imageModel as ImageModel)
                                }
                            }
                            is ShuttlePickupCargoResult.Error<*> -> {
                                view?.let { showErrorView(it) }
                            }
                            else -> {
                                // ignore
                            }
                        }
                    }
                }
            }
        }
        viewModel.addOnPropertyChangedCallback(onPropertyChangeCallback as Observable.OnPropertyChangedCallback)
    }

    private fun extractArgsFrom(savedInstanceState: Bundle?, arguments: Bundle?) {
        if (null != savedInstanceState) {
            val cargo: ShuttleParcelCargo? = savedInstanceState.getParcelable(ImageMessageType.ImageData.value)
            storedCargoId = cargo?.cargoId
        } else if (null != activity?.intent?.extras) {
            val args = activity?.intent?.extras
            val cargo: ShuttleParcelCargo? = args?.getParcelable(ImageMessageType.ImageData.value)
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

        viewModel.loadImage(shuttle, cargoId)
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
            val animatedValue: Float = animation?.animatedValue as? Float ?: FADE_OUT_END_ALPHA
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
