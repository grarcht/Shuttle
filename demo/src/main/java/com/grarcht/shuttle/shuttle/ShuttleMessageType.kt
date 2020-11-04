package com.grarcht.shuttle.shuttle

enum class ShuttleMessageType(val value: String) {
    ImageId("imageId"),
    ImageData("imageData");

    override fun toString(): String {
        return "PostalMessageType(value='$value')"
    }
}