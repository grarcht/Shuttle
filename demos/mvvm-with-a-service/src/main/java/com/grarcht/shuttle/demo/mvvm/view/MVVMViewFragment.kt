package com.grarcht.shuttle.demo.mvvm.view

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
import com.google.android.material.snackbar.Snackbar
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.mvvm.R
import com.grarcht.shuttle.demo.mvvm.viewmodel.DemoViewModel
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

private const val LOG_TAG = "MVVMViewFragment"

@AndroidEntryPoint
class MVVMViewFragment : Fragment() {
    private val viewModel by viewModels<DemoViewModel>()
    private var imageGatewayDisposableHandle: DisposableHandle? = null
    private var getImageWithoutShuttleButton: Button? = null
    private var getImageWithShuttleButton: Button? = null

    @Inject
    lateinit var shuttle: Shuttle
    var imageModel: ImageModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getImageData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.title_text).text = view.resources.getString(R.string.mvvm_view_title)
        initGetImageWithShuttleButton(view)
        initGetImageWithoutShuttleButton(view)
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
        getImageWithShuttleButton?.isEnabled = enable
        getImageWithoutShuttleButton?.isEnabled = enable
    }

    private fun getImageData() {
        imageGatewayDisposableHandle = lifecycleScope.async {
            viewModel.getImageWithLocalServiceAndWithoutUsingShuttle(this@MVVMViewFragment.context, R.raw.tower)
                ?.collectLatest {
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

    private fun getImageWithShuttle(context: Context?) {
        if (null == imageModel) {
            Log.d(LOG_TAG, "navigateWithShuttle -> The image model has not been instantiated yet.")
        } else if (null != context) {
//            val cargoId = ImageMessageType.ImageData.value
//            val startClass = MVVMViewFragment::class.java
//            val destinationClass = MVVMSecondViewActivity::class.java
//
//            shuttle.intentCargoWith(context, destinationClass)
//                .logTag(LOG_TAG)
//                .transport(cargoId, imageModel)
//                .cleanShuttleOnReturnTo(startClass, destinationClass, cargoId)
//                .deliver(context)
        }
    }

    private fun getImageWithoutShuttle(context: Context?) {
        if (null == imageModel) {
            Log.d(LOG_TAG, "navigateWithoutShuttle -> The image model has not been instantiated yet.")
        } else if (null != context) {
//            val cargoId = ImageMessageType.ImageData.value
//            val destinationClass = MVVMSecondViewActivity::class.java
//            val intent = Intent(context, destinationClass.javaClass)
//            intent.putExtra(cargoId, imageModel as Serializable)
//            context.startActivity(intent)
        }
    }

    companion object {
        const val TAG = "MVVMViewFragment"
    }
}
