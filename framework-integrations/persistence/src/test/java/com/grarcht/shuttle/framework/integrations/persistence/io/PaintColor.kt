package com.grarcht.shuttle.framework.integrations.persistence.io

import java.io.Serializable

/**
 * A minimal serializable data fixture representing a paint color, used in persistence tests to
 * provide a concrete [Serializable] payload for file-based storage and retrieval scenarios.
 */
class PaintColor(val color: String?) : Serializable {
    companion object {
        private const val serialVersionUID: Long = -84
    }
}
