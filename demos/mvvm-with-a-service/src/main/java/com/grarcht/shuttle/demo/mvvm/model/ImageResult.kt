package com.grarcht.shuttle.demo.mvvm.model

enum class ImageResult(val state: Int) {
    UNKNOWN(0),
    LOADING(1),
    SUCCESS(3),
    ERROR(4);

    companion object {
        fun getImageResult(state: Int): ImageResult {
            return when (state) {
                1 -> LOADING
                2 -> SUCCESS
                3 -> ERROR
                else -> UNKNOWN
            }
        }
    }
}