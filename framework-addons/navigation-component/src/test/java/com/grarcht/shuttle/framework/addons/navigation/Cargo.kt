package com.grarcht.shuttle.framework.addons.navigation

import com.grarcht.shuttle.framework.ShuttleCargoData

/**
 * A serializable data fixture representing a cargo shipment with an identifier and box count,
 * used across navigation-component tests as a representative payload to store, retrieve, and
 * transfer through the framework.
 */
data class Cargo(val cargoId: String, val numberOfBoxes: Int) : ShuttleCargoData {
    companion object {
        private const val serialVersionUID: Long = -53
    }
}
