package com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.viewmodel.DefaultImageLoader
import com.grarcht.shuttle.demo.core.viewmodel.ImageLoader
import kotlinx.coroutines.flow.StateFlow

class FirstViewModel : ViewModel() {

    private val imageLoader: ImageLoader = DefaultImageLoader(viewModelScope)
    val uiState: StateFlow<IOResult> = imageLoader.uiState

    fun loadImage(resources: Resources, @RawRes imageId: Int) = imageLoader.loadImage(resources, imageId)

    fun currentImageModel(): ImageModel? = imageLoader.currentImageModel()
}
