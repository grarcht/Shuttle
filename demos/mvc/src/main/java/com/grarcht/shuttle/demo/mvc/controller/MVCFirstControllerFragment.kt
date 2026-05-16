package com.grarcht.shuttle.demo.mvc.controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.grarcht.shuttle.demo.core.R
import com.grarcht.shuttle.demo.core.animation.playAnimationOverlay
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.io.RawResourceGateway
import com.grarcht.shuttle.demo.core.view.CardWithCutoutView
import com.grarcht.shuttle.demo.core.view.applySystemBarTopInset
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

private const val ERROR_UNABLE_TO_GET_IMAGE = "Unable to get the image byte array."
private const val LOG_TAG = "MVCFirstFragment"

@AndroidEntryPoint
class MVCFirstControllerFragment : Fragment() {
    @Inject
    lateinit var shuttle: Shuttle
    private var navNormallyButton: Button? = null
    private var navWithShuttleButton: Button? = null
    private var imageModel: ImageModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(com.grarcht.shuttle.demo.core.R.layout.first_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(com.grarcht.shuttle.demo.core.R.id.first_view_title_text).text =
            getString(com.grarcht.shuttle.demo.mvc.R.string.first_view_title)
        view.findViewById<CardWithCutoutView>(R.id.shuttle_card)
            ?.setCardColor(ContextCompat.getColor(view.context, R.color.colorTaupe))
        view.findViewById<CardWithCutoutView>(R.id.risky_card)
            ?.setCardColor(ContextCompat.getColor(view.context, R.color.colorBeige))
        view.applySystemBarTopInset(com.grarcht.shuttle.demo.core.R.id.content_layout)
        initOnClickNavigateWithShuttle(view)
        initOnClickNavigateNormally(view)
        getImageData()
    }

    override fun onResume() {
        super.onResume()
        enableButtons(imageModel != null)
    }

    private fun getImageData() {
        enableButtons(false)
        viewLifecycleOwner.lifecycleScope.launch {
            RawResourceGateway.with(resources)
                .logTag(LOG_TAG)
                .bytesFromRawResource(com.grarcht.shuttle.demo.core.R.raw.cargo)
                .create()
                .transformWhile { result ->
                    emit(result)
                    result !is IOResult.Success<*> && result !is IOResult.Error<*>
                }
                .collect { result ->
                    when (result) {
                        is IOResult.Unknown,
                        is IOResult.Loading -> enableButtons(false)

                        is IOResult.Success<*> -> {
                            imageModel = ImageModel(IMAGE_CARGO_ID, result.data as ByteArray)
                            enableButtons(true)
                        }

                        is IOResult.Error<*> -> {
                            val msg = result.throwable.message ?: ERROR_UNABLE_TO_GET_IMAGE
                            val snackbarView = view
                            if (snackbarView == null) {
                                Log.e(LOG_TAG, msg, result.throwable)
                            } else {
                                Snackbar.make(snackbarView, msg, Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
    }

    private fun enableButtons(enable: Boolean) {
        navWithShuttleButton?.isEnabled = enable
        navNormallyButton?.isEnabled = enable
    }

    private fun initOnClickNavigateWithShuttle(view: View?) {
        view?.apply {
            navWithShuttleButton = findViewById(R.id.nav_with_shuttle_button)
            navWithShuttleButton?.setOnClickListener {
                it.isEnabled = false
                navigateWithShuttle(context)
            }
            findViewById<ImageView>(R.id.preview_shuttle_button)?.setOnClickListener {
                playAnimation(R.raw.shuttle_delivery_success)
            }
        }
    }

    private fun initOnClickNavigateNormally(view: View?) {
        view?.apply {
            navNormallyButton = findViewById(R.id.nav_without_shuttle_button)
            navNormallyButton?.setOnClickListener {
                it.isEnabled = false
                navigateNormally(context)
            }
            findViewById<ImageView>(R.id.preview_without_shuttle_button)?.setOnClickListener {
                playAnimation(R.raw.shuttle_delivery_fail)
            }
        }
    }

    private fun playAnimation(rawResId: Int) = playAnimationThenNavigate(rawResId) {}

    private fun playAnimationThenNavigate(rawResId: Int, onComplete: () -> Unit) {
        val rootView = view as? FrameLayout ?: run {
            onComplete()
            return
        }
        rootView.playAnimationOverlay(rawResId, onComplete)
    }

    private fun navigateWithShuttle(context: Context?) {
        if (imageModel == null) {
            Log.d(LOG_TAG, "navigateWithShuttle -> The image model has not been instantiated yet.")
            return
        }
        context?.let {
            val startClass = MVCFirstControllerFragment::class.java
            val destinationClass = MVCSecondControllerActivity::class.java
            shuttle.intentCargoWith(it, destinationClass)
                .transport(IMAGE_CARGO_ID, imageModel)
                .cleanShuttleOnReturnTo(startClass, destinationClass, IMAGE_CARGO_ID)
                .deliver(it)
        }
    }

    private fun navigateNormally(context: Context?) {
        if (imageModel == null) {
            Log.d(LOG_TAG, "navigateNormally -> The image model has not been instantiated yet.")
            return
        }
        context?.let {
            val intent = Intent(it, MVCSecondControllerActivity::class.java)
            intent.putExtra(IMAGE_CARGO_ID, imageModel as Serializable)
            it.startActivity(intent)
        }
    }

    companion object {
        const val TAG = "MVCFirstFragment"
    }
}
