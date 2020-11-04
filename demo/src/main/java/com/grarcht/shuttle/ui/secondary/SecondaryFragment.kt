package com.grarcht.shuttle.ui.secondary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.grarcht.shuttle.image.ImageModel
import com.grarcht.shuttle.R
import com.grarcht.shuttle.framework.content.ShuttleDataExtractor
import com.grarcht.shuttle.framework.content.ShuttleResult
import com.grarcht.shuttle.shuttle.Shuttle
import com.grarcht.shuttle.shuttle.ShuttleMessageType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow

class SecondaryFragment : Fragment() {
    private lateinit var imageModel: ImageModel
    private var deferredImageLoad: Deferred<Unit>? = null

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

    private fun loadImageModel() {
        val repository = Shuttle.get(context as Context)

        activity?.intent?.let {
            val extractor = ShuttleDataExtractor(it, repository)

            deferredImageLoad = MainScope().async {
                extractor.extractParcelData(
                    key = ShuttleMessageType.ImageData.value,
                    parcelableCreator = ImageModel.CREATOR,
                    lifecycleOwner = this@SecondaryFragment
                ).consumeAsFlow()
                    .collect { shuttleResult ->
                        when (shuttleResult) {
                            ShuttleResult.Loading -> {
                                // TODO: show a loading view
                            }
                            is ShuttleResult.Success<*> -> {
                                hideTitle()
                                imageModel = shuttleResult.data as ImageModel
                                showImage()
                            }
                            is ShuttleResult.Error<*> -> {
                                // TODO: shown an error view
                            }
                        }
                    }
            }
        }
    }

    private fun hideTitle() {
        val titleTv = view?.findViewById<TextView>(R.id.containerTitle)
        titleTv?.visibility = View.GONE
    }

    private fun showImage() {
        val imageView = view?.findViewById<ImageView>(R.id.containerImage)
        imageView?.let { image ->
            val bitmap = getBitmap()
            bitmap?.let {
                image.setImageBitmap(it)
                image.visibility = View.VISIBLE
            }
        }
    }

    private fun getBitmap(): Bitmap? {
        return try {
            val imageBytes = imageModel.imageData
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap
        } catch (e: IllegalArgumentException) {
            null
        } catch (t: Throwable) {
            null
        }
    }
}