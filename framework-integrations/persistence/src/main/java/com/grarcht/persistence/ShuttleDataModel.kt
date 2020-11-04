package com.grarcht.persistence

/**
 *
 */
interface ShuttleDataModel {
    /**
     *
     */
    var parcelId: Int

    /**
     *
     */
    val lookupKey: String

    /**
     *
     */
    val data: ByteArray
}