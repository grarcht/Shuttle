package com.grarcht.shuttle.demo.mvvmwithaservice.view

import android.content.Context
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.grarcht.shuttle.demo.core.R.color
import com.grarcht.shuttle.demo.core.R.raw
import com.grarcht.shuttle.demo.core.animation.playAnimationOverlay
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.view.CardWithCutoutView
import com.grarcht.shuttle.demo.core.view.applySystemBarTopInset
import com.grarcht.shuttle.demo.mvvmwithaservice.R
import com.grarcht.shuttle.demo.mvvmwithaservice.viewmodel.DemoViewModel
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOG_TAG = "MVVMViewFragment"
private const val WARN_IMAGE_DATA_COMPLETION = "Caught when getting the image data."
private const val WARN_UNKNOWN_IPC_RESULT = "Unknown result when getting the image using the remote Shuttle Service (IPC)."
private const val WARN_UNKNOWN_LOCAL_RESULT = "Unknown result when getting the image using the local Shuttle Service."

/**
 * Part of the view component for displaying the views and communicating
 * with the [DemoViewModel] component to retrieve the cargo.
 */
@AndroidEntryPoint
class MVVMViewFragment : Fragment() {

    private var getImageWithoutShuttleButton: Button? = null
    private var getImageWithShuttleButton: Button? = null
    private var lceDialogFragment: LCEDialogFragment? = null
    private val viewModel by viewModels<DemoViewModel>()

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initMessaging(context?.applicationContext, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiAppearance(view)
        view.applySystemBarTopInset(R.id.content_layout)
        initGetImageWithShuttleButton(view)
        initGetImageWithoutShuttleButton(view)
        initPreviewButtons(view)
    }

    private fun initUiAppearance(view: View) {
        view.findViewById<TextView>(R.id.title_text).text = view.resources.getString(R.string.mvvm_view_title)
        view.findViewById<CardWithCutoutView>(R.id.shuttle_card)
            ?.setCardColor(ContextCompat.getColor(view.context, color.colorTaupe))
        view.findViewById<CardWithCutoutView>(R.id.risky_card)
            ?.setCardColor(ContextCompat.getColor(view.context, color.colorBeige))
    }

    private fun initPreviewButtons(view: View) {
        view.findViewById<ImageView>(R.id.preview_shuttle_button)?.setOnClickListener {
            playAnimation(raw.shuttle_delivery_success)
        }
        view.findViewById<ImageView>(R.id.preview_without_shuttle_button)?.setOnClickListener {
            playAnimation(raw.shuttle_delivery_fail)
        }
    }

    override fun onResume() {
        super.onResume()
        enableButtons(true)
    }

    override fun onStop() {
        enableButtons(false)
        super.onStop()
    }

    private fun enableButtons(enable: Boolean) {
        getImageWithShuttleButton?.isEnabled = enable
        getImageWithoutShuttleButton?.isEnabled = enable
    }

    private fun initGetImageWithShuttleButton(view: View?) {
        view?.apply {
            getImageWithShuttleButton = findViewById(R.id.nav_with_shuttle_button)
            getImageWithShuttleButton?.setOnClickListener {
                it.isEnabled = false
                getImageWithShuttle(context)
            }
        }
    }

    private fun initGetImageWithoutShuttleButton(view: View?) {
        view?.apply {
            getImageWithoutShuttleButton = findViewById(R.id.nav_without_shuttle_button)
            getImageWithoutShuttleButton?.setOnClickListener {
                it.isEnabled = false
                getImageWithoutShuttle(context)
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun loadImageCargo(
        context: Context?,
        flowProvider: (Context, String, Int) -> Flow<IOResult>?,
        unknownResultLogTag: String
    ) {
        context?.applicationContext?.let { appContext ->
            val cargoId = IMAGE_CARGO_ID
            val imageId = raw.cargo
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    flowProvider(appContext, cargoId, imageId)?.collectLatest {
                        when (it) {
                            IOResult.Loading -> showLoadingDialog()
                            is IOResult.Success<*> -> showSuccessContentDialog(it.data as? ImageModel ?: return@collectLatest)
                            is IOResult.Error<*> -> showErrorDialog(it.message, it.throwable)
                            IOResult.Unknown -> Log.w(LOG_TAG, unknownResultLogTag)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(LOG_TAG, WARN_IMAGE_DATA_COMPLETION, e)
                }
            }
        }
    }

    private fun getImageWithoutShuttle(context: Context?) =
        loadImageCargo(context, viewModel::transportImageCargoWithoutUsingShuttle, WARN_UNKNOWN_LOCAL_RESULT)

    private fun getImageWithShuttle(context: Context?) =
        loadImageCargo(context, viewModel::transportImageCargoUsingShuttleAndIPC, WARN_UNKNOWN_IPC_RESULT)

    private fun observeDialogDismissal() {
        val dialog = lceDialogFragment ?: return
        dialog.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                enableButtons(true)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    private fun showLoadingDialog() {
        if (lceDialogFragment?.dialogType == DialogType.LOADING) {
            return
        }

        lceDialogFragment = LCEDialogFragment.createLoadingDialogWith()
        lceDialogFragment?.show(parentFragmentManager, LCEDialogFragment.TAG_LCE_LOADING)
    }

    private fun showSuccessContentDialog(imageModel: ImageModel) {
        if (lceDialogFragment?.dialogType == DialogType.CONTENT) {
            return
        }

        val previousDialogFragment = lceDialogFragment
        lceDialogFragment = LCEDialogFragment.createContentDialogWith(imageModel)
        observeDialogDismissal()
        lceDialogFragment?.show(parentFragmentManager, LCEDialogFragment.TAG_LCE_CONTENT)
        previousDialogFragment?.fadeOutView(true)
    }

    private fun showErrorDialog(errorMessage: String?, throwable: Throwable?) {
        if (lceDialogFragment?.dialogType == DialogType.ERROR ||
            errorMessage == null && throwable == null
        ) {
            return
        }

        val previousDialogFragment = lceDialogFragment
        val thrown = throwable?.message ?: ""
        val message = "$errorMessage. $thrown"
        lceDialogFragment = LCEDialogFragment.createErrorDialogWith(message)
        observeDialogDismissal()
        lceDialogFragment?.show(parentFragmentManager, LCEDialogFragment.TAG_LCE_ERROR)
        previousDialogFragment?.fadeOutView(true)
    }

    private fun playAnimation(rawResId: Int) = playAnimationThenNavigate(rawResId) {}

    private fun playAnimationThenNavigate(rawResId: Int, onComplete: () -> Unit) {
        val rootView = view as? FrameLayout ?: run {
            onComplete()
            return
        }
        rootView.playAnimationOverlay(rawResId, onComplete)
    }

    companion object {
        const val TAG = "MVVMViewFragment"
    }
}
