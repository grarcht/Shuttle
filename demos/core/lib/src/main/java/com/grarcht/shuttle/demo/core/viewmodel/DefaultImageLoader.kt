package com.grarcht.shuttle.demo.core.viewmodel

import android.content.res.Resources
import androidx.annotation.RawRes
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.io.RawResourceGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch

class DefaultImageLoader(private val scope: CoroutineScope) : ImageLoader {

    private val _uiState = MutableStateFlow<IOResult>(IOResult.Loading)
    override val uiState: StateFlow<IOResult> = _uiState.asStateFlow()

    override fun loadImage(resources: Resources, @RawRes imageId: Int) {
        if (_uiState.value is IOResult.Success<*>) return
        scope.launch {
            RawResourceGateway.with(resources)
                .bytesFromRawResource(imageId)
                .create()
                .transformWhile { result ->
                    emit(result)
                    result !is IOResult.Success<*> && result !is IOResult.Error<*>
                }
                .collect { result ->
                    if (result is IOResult.Success<*>) {
                        _uiState.value = IOResult.Success(
                            ImageModel(IMAGE_CARGO_ID, result.data as ByteArray)
                        )
                    } else {
                        _uiState.value = result
                    }
                }
        }
    }

    override fun currentImageModel(): ImageModel? =
        (_uiState.value as? IOResult.Success<*>)?.data as? ImageModel
}
