package com.grarcht.shuttle.framework

import java.io.Serializable

data class Cargo(val cargoId: String, val numberOfBoxes: Int) : Serializable {
    companion object {
        const val serialVersionUID = -53L
    }
}
