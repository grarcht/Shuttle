package com.grarcht.shuttle.framework.integrations.persistence.io

import java.io.Serializable

class PaintColor(val color: String?) : Serializable {
    companion object {
        const val serialVersionUID = -84L
    }
}
