package com.grarcht.shuttle.demo.mvvmwithaservice.model

/**
 * Denotes the image retrieval state in the [RemoteService] and [Receiver].
 */
enum class ImageResult(val state: Int) {
    UNKNOWN(0),
    LOADING(1),
    SUCCESS(3),
    ERROR(4);

    companion object {
        /**
         * Maps the [state] to the [ImageResult].
         *
         * @return the mapped [ImageResult].
         */
        fun getImageResult(state: Int): ImageResult {
            return when (state) {
                LOADING.state -> LOADING
                SUCCESS.state -> SUCCESS
                ERROR.state -> ERROR
                else -> UNKNOWN
            }
        }
    }
}