package com.grarcht.shuttle.framework.content.bundle

import android.os.Parcelable
import java.io.Serializable

data class SerializableParcelable(val parcelable: Parcelable) : Serializable {
    companion object {
        const val serialVersionUID = -32453L
    }
}
