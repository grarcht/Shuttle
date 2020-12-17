package com.grarcht.shuttle.demo.shuttle

import android.content.Context
import com.grarcht.persistence.ShuttleDatabaseBlobAdapter
import com.grarcht.shuttle.framework.respository.ShuttleWarehouse
import com.grarcht.room.ShuttleDataDb
import com.grarcht.room.RoomShuttleDataModelFactory
import com.grarcht.shuttle.framework.respository.ShuttleRepository

object Shuttle {
    private var parcelRepository: ShuttleWarehouse? = null

    fun get(context: Context): ShuttleWarehouse {
        if (null == parcelRepository) {
            // Create the database and dao using an extension
            val db = ShuttleDataDb.getInstance(context.applicationContext)
            val dao = db.parcelDataAccessObject

            // Create the repository for interaction
            val blobAdapter = ShuttleDatabaseBlobAdapter()
            val parcelDataModelFactory = RoomShuttleDataModelFactory(blobAdapter)
            parcelRepository = ShuttleRepository(dao, parcelDataModelFactory, blobAdapter)
        }
        @Suppress("UNCHECKED_CAST")
        return parcelRepository as ShuttleWarehouse
    }
}