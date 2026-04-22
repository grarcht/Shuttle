package com.grarcht.shuttle.framework.bundle

import java.io.Serializable

/**
 * A minimal serializable data fixture representing a paint color, used in bundle and serialization
 * tests to provide a concrete [Serializable] payload.
 */
class PaintColor(val color: String?) : Serializable {
    companion object {
        private const val serialVersionUID: Long = -84
    }
}
