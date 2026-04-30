package com.grarcht.shuttle.framework

/**
 * A cargo data fixture representing a cargo shipment with an identifier and box count, used
 * across shuttle tests as a representative payload to store, retrieve, and transfer through
 * the framework.
 */
data class Cargo(val cargoId: String, val numberOfBoxes: Int) : ShuttleCargoData {
    companion object {
        private const val serialVersionUID: Long = -53
    }
}
