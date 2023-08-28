package com.grarcht.shuttle.framework.addons.navigation

import java.io.Serializable

data class Cargo(val cargoId: String, val numberOfBoxes: Int) : Serializable {
    companion object {
        private const val serialVersionUID: Long = -53
    }
}
