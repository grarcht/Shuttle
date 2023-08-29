package com.grarcht.shuttle.framework.content.bundle

import android.os.Parcelable
import java.io.Serializable

data class SerializableParcelable(val parcelable: Parcelable) : Serializable {
    companion object {
        private const val serialVersionUID: Long = -32453
    }
}
