package com.grarcht.shuttle.framework

import java.io.Serializable

/**
 * A serializable data fixture representing a cargo shipment with an identifier and box count,
 * used across shuttle tests as a representative payload to store, retrieve, and transfer through
 * the framework.
 */
data class Cargo(val cargoId: String, val numberOfBoxes: Int) : Serializable {
    companion object {
        private const val serialVersionUID: Long = -53
    }
}
