package com.grarcht.shuttle.demo.mvvm.view

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
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.os.getParcelableWith
import com.grarcht.shuttle.demo.core.view.applyBiasedCrop
import com.grarcht.shuttle.demo.core.view.applyNavBarInsetToCard
import com.grarcht.shuttle.demo.core.view.hideLoadingView
import com.grarcht.shuttle.demo.core.view.setImageSizeText
import com.grarcht.shuttle.demo.core.view.showErrorView
import com.grarcht.shuttle.demo.mvvm.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOG_TAG = "MVVMSecondViewFragment"

@AndroidEntryPoint
class MVVMSecondViewFragment : Fragment() {

    private lateinit var contentLoadingProgressBar: ContentLoadingProgressBar
    private var hideLoadingViewAnimator: ObjectAnimator? = null
    private val viewModel by viewModels<SecondViewModel>()
    private var imageModel: ImageModel? = null
    private var storedCargoId: String? = null

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(com.grarcht.shuttle.demo.core.R.layout.second_view, container, false)
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
            .transport(IMAGE_CARGO_ID, imageModel)
            .create()
        super.onSaveInstanceState(outStateShuttleBundle)
    }

    override fun onDestroyView() {
        hideLoadingViewAnimator?.cancel()
        contentLoadingProgressBar.hide()
        super.onDestroyView()
    }

    private fun extractArgsFrom(savedInstanceState: Bundle?, arguments: Bundle?) {
        val bundle: Bundle? = savedInstanceState ?: arguments
        bundle?.let {
            val cargo: ShuttleParcelCargo? =
                it.getParcelableWith(IMAGE_CARGO_ID, ShuttleParcelCargo::class.java)
            storedCargoId = cargo?.cargoId
        }
    }

    private fun loadImageModel() {
        imageModel?.let {
            showSuccessView(view, it)
            return
        }

        val cargoId = storedCargoId ?: ""
        if (cargoId.isEmpty()) {
            hideLoadingViewAnimator = showErrorView(view)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel
                .loadImage(shuttle, cargoId)
                .collectLatest { shuttleResult ->
                    when (shuttleResult) {
                        is ShuttlePickupCargoResult.Loading -> {
                            view?.let { initLoadingView(it) }
                        }

                        is ShuttlePickupCargoResult.Success<*> -> {
                            val model = shuttleResult.data as? ImageModel ?: return@collectLatest
                            imageModel = model
                            view?.let { showSuccessView(view, model) }
                            cancel()
                        }

                        is ShuttlePickupCargoResult.Error<*> -> {
                            view?.let { hideLoadingViewAnimator = showErrorView(it) }
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
        contentLoadingProgressBar = view.findViewById(com.grarcht.shuttle.demo.core.R.id.loading_indicator)
        contentLoadingProgressBar.show()
    }

    private fun showSuccessView(view: View?, imageModel: ImageModel) {
        this.imageModel = imageModel
        val successLayout = view?.findViewById<FrameLayout>(com.grarcht.shuttle.demo.core.R.id.successLayout) ?: return
        val imageView = successLayout.findViewById<ImageView>(com.grarcht.shuttle.demo.core.R.id.retrievedImage)
        val bitmap = BitmapDecoder.decodeBitmap(imageModel.imageData) ?: return
        applyBiasedCrop(imageView, bitmap)
        setImageSizeText(successLayout, imageModel.imageData.size.toLong())
        applyNavBarInsetToCard(successLayout)
        successLayout.alpha = 0f
        successLayout.visibility = View.VISIBLE
        hideLoadingViewAnimator = hideLoadingView(view, successLayout)
    }
}
