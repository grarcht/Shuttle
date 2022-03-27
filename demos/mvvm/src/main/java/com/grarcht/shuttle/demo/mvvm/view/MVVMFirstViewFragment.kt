package com.grarcht.shuttle.demo.mvvm.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.mvvm.R
import com.grarcht.shuttle.demo.mvvm.viewmodel.FirstViewModel
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import java.io.Serializable
import javax.inject.Inject

private const val LOG_TAG = "MVVMFirstViewFragment"

@AndroidEntryPoint
class MVVMFirstViewFragment : Fragment() {
    private val viewModel by viewModels<FirstViewModel>()
    private var imageGatewayDisposableHandle: DisposableHandle? = null

    private var navNormallyButton: Button? = null
    private var navWithShuttleButton: Button? = null

    @Inject
    lateinit var shuttle: Shuttle
    var imageModel: ImageModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getImageData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.first_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.first_view_title_text).text =
            view.resources.getString(R.string.mvvm_first_view_title)
        initOnClickNavigateWithShuttle(view)
        initOnClickNavigateNormally(view)
        getImageData()
    }

    override fun onResume() {
        super.onResume()
        enableButtons(true)
    }

    override fun onDestroyView() {
        imageGatewayDisposableHandle?.dispose()
        super.onDestroyView()
    }

    private fun enableButtons(enable: Boolean) {
        navWithShuttleButton?.isEnabled = enable
        navNormallyButton?.isEnabled = enable
    }

    private fun getImageData() {
        imageGatewayDisposableHandle = MainScope().async {
            viewModel.getImage(resources, R.raw.tower)
                .collectLatest {
                    when (it) {
                        is IOResult.Unknown,
                        is IOResult.Loading -> {
                            enableButtons(false)
                        }
                        is IOResult.Success<*> -> {
                            val byteArray = it.data as ByteArray
                            imageModel = ImageModel(ImageMessageType.ImageData.value, byteArray)
                            enableButtons(true)
                            cancel()
                        }
                        is IOResult.Error<*> -> {
                            val errorMessage = it.throwable.message ?: "Unable to get the image byte array."

                            if (null == view) {
                                Log.e(TAG, errorMessage, it.throwable)
                            } else {
                                Snackbar.make(view as View, errorMessage, Snackbar.LENGTH_SHORT).show()
                            }
                            cancel()
                        }
                    }
                }
        }.invokeOnCompletion {
            it?.let {
                Log.w(TAG, "Caught when getting the image model.", it)
            }
        }
    }

    private fun initOnClickNavigateWithShuttle(view: View?) {
        view?.apply {
            navWithShuttleButton = findViewById(R.id.nav_with_shuttle_button)
            navWithShuttleButton?.setOnClickListener {
                it.isEnabled = false
                navigateWithShuttle(context)
            }
        }
    }

    private fun initOnClickNavigateNormally(view: View?) {
        view?.apply {
            navNormallyButton = findViewById(R.id.nav_normally_button)
            navNormallyButton?.setOnClickListener {
                it.isEnabled = false
                navigateNormally(context)
            }
        }
    }

    private fun navigateWithShuttle(context: Context?) {
        if (null == imageModel) {
            Log.d(LOG_TAG, "navigateWithShuttle -> The image model has not been instantiated yet.")
        } else if (null != context) {
            val cargoId = ImageMessageType.ImageData.value
            val startClass = MVVMFirstViewFragment::class.java
            val destinationClass = MVVMSecondViewActivity::class.java

            shuttle.intentCargoWith(context, destinationClass)
                .logTag(LOG_TAG)
                .transport(cargoId, imageModel)
                .cleanShuttleOnReturnTo(startClass, destinationClass, cargoId)
                .deliver(context)
        }
    }

    private fun navigateNormally(context: Context?) {
        if (null == imageModel) {
            Log.d(LOG_TAG, "navigateNormally -> The image model has not been instantiated yet.")
        } else if (null != context) {
            val cargoId = ImageMessageType.ImageData.value
            val destinationClass = MVVMSecondViewActivity::class.java
            val intent = Intent(context, destinationClass.javaClass)
            intent.putExtra(cargoId, imageModel as Serializable)
            context.startActivity(intent)
        }
    }

    companion object {
        const val TAG = "MVVMFirstViewFragment"
    }
}
