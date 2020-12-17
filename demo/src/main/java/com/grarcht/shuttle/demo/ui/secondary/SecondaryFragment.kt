package com.grarcht.shuttle.demo.ui.secondary

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.grarcht.shuttle.demo.image.ImageModel
import com.grarcht.shuttle.R
import com.grarcht.shuttle.demo.image.BitmapDecoder
import com.grarcht.shuttle.framework.content.ShuttleDataExtractor
import com.grarcht.shuttle.framework.content.ShuttleResult
import com.grarcht.shuttle.demo.shuttle.Shuttle
import com.grarcht.shuttle.demo.shuttle.ShuttleDataExtractorFactory
import com.grarcht.shuttle.demo.shuttle.ShuttleMessageType
import com.grarcht.shuttle.framework.content.bundle.ShuttleBundle
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow


class SecondaryFragment : Fragment() {
    private val bitmapDecoder = BitmapDecoder()
    private lateinit var imageModel: ImageModel
    private var deferredImageLoad: Deferred<Unit>? = null
    private val repository = Shuttle.get(context as Context)
    private val shuttleDataExtractorFactory = ShuttleDataExtractorFactory()
    private lateinit var shuttleDataExtractor: ShuttleDataExtractor

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadImageModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.secondary_fragment, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Saving the state in a guarded fashion.  The super call must have the shuttle bundle to ensure apps don't
        // crash during transactions with saving and restoring states.
        val outStateShuttleBundle = ShuttleBundle.with(
            bundle = outState,
            repository = repository,
            dataExtractor = shuttleDataExtractor).create()
        super.onSaveInstanceState(outStateShuttleBundle)
    }

    private fun loadImageModel() {
        activity?.intent?.let {
            val dataExtractor = shuttleDataExtractorFactory.create(it, repository)

            deferredImageLoad = MainScope().async {
                getShuttleChannel(dataExtractor)
                    .consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            ShuttleResult.Loading -> showLoadingView()
                            is ShuttleResult.Success<*> -> showSuccessView(shuttleResult)
                            is ShuttleResult.Error<*> -> showErrorView()
                        }
                    }
            }
        }
    }

    private suspend fun getShuttleChannel(dataExtractor: ShuttleDataExtractor): Channel<ShuttleResult> {
        return dataExtractor.extractParcelData(
            bundle = activity?.intent?.extras,
            key = ShuttleMessageType.ImageData.value,
            parcelableCreator = ImageModel.CREATOR,
            lifecycleOwner = this@SecondaryFragment
        )
    }

    private fun showLoadingView() {

    }

    private fun showErrorView() {

    }

    private fun showSuccessView(shuttleResult: ShuttleResult.Success<*>) {
        hideTitle()
        imageModel = shuttleResult.data as ImageModel
        showImage()
    }

    private fun hideTitle() {
        val titleTv = view?.findViewById<TextView>(R.id.containerTitle)
        titleTv?.visibility = View.GONE
    }

    private fun showImage() {
        val imageView = view?.findViewById<ImageView>(R.id.containerImage)
        imageView?.let { image ->
            val bitmap = bitmapDecoder.decodeBitmap(imageModel.imageData)
            bitmap?.let {
                image.setImageBitmap(it)
                image.visibility = View.VISIBLE
            }
        }
    }
}