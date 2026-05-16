package com.grarcht.shuttle.demo.core.viewmodel

import android.content.res.Resources
import androidx.annotation.RawRes
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import kotlinx.coroutines.flow.StateFlow

interface ImageLoader {
    val uiState: StateFlow<IOResult>
    fun loadImage(resources: Resources, @RawRes imageId: Int)
    fun currentImageModel(): ImageModel?
}
