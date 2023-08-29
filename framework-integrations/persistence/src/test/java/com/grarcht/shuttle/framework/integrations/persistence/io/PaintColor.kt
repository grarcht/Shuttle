package com.grarcht.shuttle.framework.integrations.persistence.io

import java.io.Serializable

class PaintColor(val color: String?) : Serializable {
    companion object {
        private const val serialVersionUID: Long = -84
    }
}
