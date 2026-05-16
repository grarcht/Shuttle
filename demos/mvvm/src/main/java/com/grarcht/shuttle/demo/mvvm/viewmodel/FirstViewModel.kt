package com.grarcht.shuttle.demo.mvvm.viewmodel

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.viewmodel.DefaultImageLoader
import com.grarcht.shuttle.demo.core.viewmodel.ImageLoader
import kotlinx.coroutines.flow.StateFlow

data class NavigationCargo(val cargoId: String, val imageModel: ImageModel)

class FirstViewModel : ViewModel() {

    private val imageLoader: ImageLoader = DefaultImageLoader(viewModelScope)
    val uiState: StateFlow<IOResult> = imageLoader.uiState

    fun loadImage(resources: Resources, @RawRes imageId: Int) = imageLoader.loadImage(resources, imageId)

    fun currentImageModel(): ImageModel? = imageLoader.currentImageModel()

    fun navigationCargo(): NavigationCargo? =
        currentImageModel()?.let { NavigationCargo(IMAGE_CARGO_ID, it) }
}
