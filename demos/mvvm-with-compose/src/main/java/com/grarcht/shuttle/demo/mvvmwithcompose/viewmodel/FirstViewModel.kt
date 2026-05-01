package com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.io.RawResourceGateway
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The MVVM ViewModel used with the First View. Owns image-loading state so the view
 * only observes and renders — no coroutine management in the view layer.
 */
class FirstViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<IOResult>(IOResult.Loading)
    val uiState: StateFlow<IOResult> = _uiState.asStateFlow()

    fun loadImage(resources: Resources, @RawRes imageId: Int) {
        viewModelScope.launch {
            RawResourceGateway.with(resources)
                .bytesFromRawResource(imageId)
                .create()
                .collectLatest { result ->
                    when (result) {
                        is IOResult.Success<*> -> {
                            val bytes = result.data as ByteArray
                            _uiState.value = IOResult.Success(
                                ImageModel(ImageMessageType.ImageData.value, bytes)
                            )
                            cancel()
                        }
                        else -> _uiState.value = result
                    }
                }
        }
    }

    fun currentImageModel(): ImageModel? =
        (_uiState.value as? IOResult.Success<*>)?.data as? ImageModel
}
