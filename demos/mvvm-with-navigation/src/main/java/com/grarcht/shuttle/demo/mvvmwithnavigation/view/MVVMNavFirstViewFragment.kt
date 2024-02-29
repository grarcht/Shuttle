package com.grarcht.shuttle.demo.mvvmwithnavigation.view

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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.mvvmwithnavigation.R
import com.grarcht.shuttle.demo.mvvmwithnavigation.viewmodel.FirstViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.addons.navigation.navigateWithShuttle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import java.io.Serializable
import javax.inject.Inject

private const val LOG_TAG = "MVVMFirstViewFragment"

@AndroidEntryPoint
class MVVMNavFirstViewFragment : Fragment() {
    private val viewModel by viewModels<FirstViewModel>()
    private var imageGatewayDisposableHandle: DisposableHandle? = null
    private var imageModel: ImageModel? = null
    private var navController: NavController? = null
    private var navNormallyButton: Button? = null
    private var navWithShuttleButton: Button? = null

    @Inject
    lateinit var shuttle: Shuttle


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.first_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNavController()
        view.findViewById<TextView>(R.id.first_view_title_text).text =
            view.resources.getString(R.string.mvvm_first_view_with_navigation_title)
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

    private fun initNavController() {
        var navController: NavController? = null

        view?.let {
            try {
                navController = Navigation.findNavController(it)
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "")
            }
        }

        this.navController = navController
    }

    private fun getImageData() {
        imageGatewayDisposableHandle = MainScope().async {
            viewModel.getImage(resources, R.raw.tower)
                .collectLatest {
                    when (it) {
                        is IOResult.Loading -> {
                            enableButtons(false)
                        }
                        is IOResult.Success<*> -> {
                            val byteArray = it.data as ByteArray
                            imageModel = ImageModel(ImageMessageType.ImageData.value, byteArray)
                            enableButtons(true)
                        }
                        is IOResult.Error<*> -> {
                            val errorMessage = it.throwable.message ?: "Unable to get the image byte array."

                            if (null == view) {
                                Log.e(LOG_TAG, errorMessage, it.throwable)
                            } else {
                                Snackbar.make(view as View, errorMessage, Snackbar.LENGTH_SHORT).show()
                            }
                        }
                        else -> {
                            // ignore
                        }
                    }
                }
        }.invokeOnCompletion {
            it?.let {
                Log.w(LOG_TAG, "Caught when getting the image model.", it)
            }
        }
    }

    private fun initOnClickNavigateWithShuttle(view: View?) {
        view?.apply {
            navWithShuttleButton = findViewById(R.id.nav_with_shuttle_button)
            navWithShuttleButton?.setOnClickListener {
                it.isEnabled = false
                navigateWithShuttleAndTheNavController(context)
            }
        }
    }

    private fun initOnClickNavigateNormally(view: View?) {
        view?.apply {
            navNormallyButton = findViewById(R.id.nav_without_shuttle_button)
            navNormallyButton?.setOnClickListener {
                it.isEnabled = false
                navigateNormallyWithTheNavController(context)
            }
        }
    }

    private fun navigateWithShuttleAndTheNavController(context: Context?) {
        if (null == imageModel) {
            Log.d(LOG_TAG, "navigateWithShuttleAndTheNavigator -> The image model has not been instantiated yet.")
        } else if (null != context) {
            val cargoId = ImageMessageType.ImageData.value
            val startClass = MVVMNavFirstViewFragment::class.java
            val destinationClass = MVVMNavSecondViewActivity::class.java

            navController.navigateWithShuttle(shuttle, R.id.MVVMNavSecondViewActivity)
                ?.logTag(LOG_TAG)
                ?.transport(cargoId, imageModel as Serializable)
                ?.cleanShuttleOnReturnTo(startClass, destinationClass, cargoId)
                ?.deliver()
        }
    }

    private fun navigateNormallyWithTheNavController(context: Context?) {
        if (null == imageModel) {
            Log.d(LOG_TAG, "navigateNormally -> The image model has not been instantiated yet.")
        } else if (null != context) {
            val cargoId = ImageMessageType.ImageData.value
            val args = Bundle()
            args.putSerializable(cargoId, imageModel as Serializable)
            navController?.navigate(R.id.MVVMNavSecondViewActivity, args)
        }
    }
}
