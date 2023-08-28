package com.grarcht.shuttle.framework.integrations.extensions.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDatabase

/**
 * This class is used to create and access the Shuttle database.
 */
@Database(entities = [ShuttleRoomData::class], version = 1, exportSchema = false)
abstract class ShuttleRoomDataDb : RoomDatabase(), ShuttleDatabase {
    /**
     * The Data Access Object (DAO) reference for the DAO used to access the Shuttle database.
     */
    override val shuttleDataAccessObject: ShuttleDataAccessObject by lazy {
        ShuttleRoomDao(getShuttleDao())
    }

    /**
     * @return the Data Access Object (DAO) reference
     * @see [shuttleDataAccessObject]
     */
    internal abstract fun getShuttleDao(): ShuttleRoomDao.Dao

    companion object {
        @Volatile
        private var INSTANCE: ShuttleRoomDataDb? = null

        fun getInstance(config: ShuttleRoomDbConfig): ShuttleRoomDataDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ShuttleRoomDbFactory().createDb(config).also { INSTANCE = it }
            }
        }
    }
}
