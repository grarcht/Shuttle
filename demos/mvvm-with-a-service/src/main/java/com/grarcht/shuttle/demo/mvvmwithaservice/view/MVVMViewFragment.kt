package com.grarcht.shuttle.demo.mvvmwithaservice.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.mvvm.R
import com.grarcht.shuttle.demo.mvvmwithaservice.viewmodel.DemoViewModel
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

private const val LOG_TAG = "MVVMViewFragment"

/**
 * Part of the view component for displaying the views and communicating with the [DemoViewModel] component to retrieve the cargo.
 */
@AndroidEntryPoint
class MVVMViewFragment : Fragment() {

    private var getImageWithoutShuttleButton: Button? = null
    private var getImageWithShuttleButton: Button? = null
    private var lceDialogFragment: LCEDialogFragment? = null
    private val viewModel by viewModels<DemoViewModel>()

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initMessaging(context, lifecycle)
        view.findViewById<TextView>(R.id.title_text).text = view.resources.getString(R.string.mvvm_view_title)
        initGetImageWithShuttleButton(view)
        initGetImageWithoutShuttleButton(view)
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

    private fun getImageWithoutShuttle(context: Context?) {
        if (null != context) {
            val cargoId = ImageMessageType.ImageData.value

            lifecycleScope.async {
                val towerImageId: Int = com.grarcht.shuttle.demo.core.R.raw.tower
                viewModel.transportImageCargoWithoutUsingShuttle(context, cargoId, towerImageId)?.collectLatest {
                    when (it) {
                        IOResult.Loading -> showLoadingDialog()
                        is IOResult.Success<*> -> showSuccessContentDialog(it.data as ImageModel)
                        is IOResult.Error<*> -> showErrorDialog(it.message, it.throwable)
                        IOResult.Unknown -> {
                            Log.w(LOG_TAG, "Unknown result when getting the image using the local Shuttle Service.")
                        }
                    }
                }
            }.invokeOnCompletion {
                it?.let {
                    Log.w(TAG, "Caught when getting the image data.", it)
                }
            }
        }
    }

    private fun getImageWithShuttle(context: Context?) {
        if (null != context) {
            val cargoId = ImageMessageType.ImageData.value

            lifecycleScope.async {
                val towerImageId: Int = com.grarcht.shuttle.demo.core.R.raw.tower
                viewModel.transportImageCargoUsingShuttleAndIPC(context, cargoId, towerImageId)?.collectLatest {
                    when (it) {
                        IOResult.Loading -> showLoadingDialog()
                        is IOResult.Success<*> -> showSuccessContentDialog(it.data as ImageModel)
                        is IOResult.Error<*> -> showErrorDialog(it.message, it.throwable)
                        IOResult.Unknown -> {
                            Log.w(LOG_TAG, "Unknown result when getting the image using the remote Shuttle Service (IPC).")
                        }
                    }
                }
            }.invokeOnCompletion {
                it?.let {
                    Log.w(TAG, "Caught when getting the image data.", it)
                }
            }
        }
    }

    private fun setDialogOnDismissListener() {
        lceDialogFragment?.setOnDismissListener {
            enableButtons(true)
            lceDialogFragment?.setOnDismissListener(null)
        }
    }

    private fun showLoadingDialog() {
        if (lceDialogFragment?.dialogType == DialogType.LOADING) {
            return
        }

        lceDialogFragment = LCEDialogFragment.createLoadingDialogWith()
        setDialogOnDismissListener()
        lceDialogFragment?.show(parentFragmentManager, LCEDialogFragment.TAG_LCE_LOADING)
    }

    private fun showSuccessContentDialog(imageModel: ImageModel) {
        if (lceDialogFragment?.dialogType == DialogType.CONTENT) {
            return
        }

        val previousDialogFragment = lceDialogFragment
        lceDialogFragment = LCEDialogFragment.createContentDialogWith(imageModel)
        setDialogOnDismissListener()
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
        val message = "${errorMessage}. $thrown"
        lceDialogFragment = LCEDialogFragment.createErrorDialogWith(message)
        setDialogOnDismissListener()
        lceDialogFragment?.show(parentFragmentManager, LCEDialogFragment.TAG_LCE_ERROR)
        previousDialogFragment?.fadeOutView(true)
    }

    companion object {
        const val TAG = "MVVMViewFragment"
    }
}
