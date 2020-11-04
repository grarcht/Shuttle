package com.grarcht.shuttle.ui.main

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import com.grarcht.shuttle.R
import com.grarcht.shuttle.image.ImageModel
import com.grarcht.shuttle.ui.secondary.SecondActivity
import com.grarcht.shuttle.framework.content.ShuttleIntent
import com.grarcht.shuttle.io.IOResult
import com.grarcht.shuttle.io.ImageGateway
import com.grarcht.shuttle.shuttle.Shuttle
import com.grarcht.shuttle.shuttle.ShuttleMessageType

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class MainFragment : Fragment() {
    private val logTag = MainFragment::class.java.simpleName
    private var imageGatewayDisposableHandle: DisposableHandle? = null
    private var imageModel: ImageModel? = null
    private var navSafelyButton: Button? = null
    private var navNormallyButton: Button? = null
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        loadByteArray()
        initOnClickNavigateSafely()
        initOnClickNavigateNormally()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        imageGatewayDisposableHandle?.dispose()
        super.onDestroyView()
    }

    private fun enableButtons(enable: Boolean) {
        navSafelyButton?.isEnabled = enable
        navNormallyButton?.isEnabled = enable
    }

    private fun loadByteArray() {
        imageGatewayDisposableHandle = MainScope().async {
            val result = ImageGateway.with(resources)
                .bytesFromRawResource(R.raw.tower)
                .create()

            result.collect {
                when (it) {
                    IOResult.Loading -> {
                        enableButtons(false)
                    }
                    is IOResult.Success<*> -> {
                        val byteArray = it.data as ByteArray
                        imageModel = ImageModel(ShuttleMessageType.ImageData.value, byteArray)
                        enableButtons(true)
                    }
                    is IOResult.Error<*> -> {
                        val errorMessage = it.throwable.message ?: "Unable to get the image byte array."

                        if (null == view) {
                            Log.e(logTag, errorMessage, it.throwable)
                        } else {
                            Snackbar.make(view as View, errorMessage, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }.invokeOnCompletion {

        }
    }

    private fun initOnClickNavigateSafely() {
        navSafelyButton = view?.findViewById<Button>(R.id.navSafelyBtn)
        navSafelyButton?.setOnClickListener {
            navigateSafelyToNextActivityWithIdForLargeObject()
        }
    }

    private fun initOnClickNavigateNormally() {
        navNormallyButton = view?.findViewById<Button>(R.id.navNormallyBtn)
        navNormallyButton?.setOnClickListener {
            navigateToNextActivityWithLargeObject()
        }
    }

    private fun navigateSafelyToNextActivityWithIdForLargeObject() {
        if (null == imageModel) {
            Log.d(logTag, "The image model has not been instantiated yet.")
        } else {
            val repository = Shuttle.get(context as Context)
            val intent = ShuttleIntent.with(context, SecondActivity::class.java, repository)
                ?.safelyPutExtra(ShuttleMessageType.ImageData.value, imageModel)
                ?.complete()
            startActivity(intent)
        }
    }

    private fun navigateToNextActivityWithLargeObject() {
        if (null == imageModel) {
            Log.d(logTag, "The image model has not been instantiated yet.")
        } else {
            val intent = Intent(context, SecondActivity::class.java)
                .putExtra(ShuttleMessageType.ImageData.value, imageModel)
            startActivity(intent)
        }
    }
}