package com.grarcht.shuttle.framework.bundle

import com.grarcht.shuttle.framework.ShuttleCargoData

/**
 * A minimal cargo data fixture representing a paint color, used in bundle and serialization
 * tests to provide a concrete [ShuttleCargoData] payload.
 */
class PaintColor(val color: String?) : ShuttleCargoData {
    companion object {
        private const val serialVersionUID: Long = -84
    }
}
