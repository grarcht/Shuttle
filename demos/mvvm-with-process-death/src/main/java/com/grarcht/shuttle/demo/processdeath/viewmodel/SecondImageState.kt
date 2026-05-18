package com.grarcht.shuttle.demo.processdeath.viewmodel

import com.grarcht.shuttle.demo.core.image.ImageModel

sealed class SecondImageState {
    object Loading : SecondImageState()
    data class Success(val imageModel: ImageModel) : SecondImageState()
    object LostToProcessDeath : SecondImageState()
    data class Error(val message: String) : SecondImageState()
}
