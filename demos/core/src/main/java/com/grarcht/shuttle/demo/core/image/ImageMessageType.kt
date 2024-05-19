package com.grarcht.shuttle.demo.core.image

enum class ImageMessageType(val value: String) {
    ImageData("imageData500");

    override fun toString(): String {
        return "PostalMessageType(value='$value')"
    }
}
