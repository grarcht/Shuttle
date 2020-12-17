package com.grarcht.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.grarcht.persistence.ShuttleDataAccessObject
import com.grarcht.persistence.ShuttleDatabase

private const val DB_NAME = "Shuttle.db"

@Database(entities = [ShuttleData::class], version = 1, exportSchema = false)
abstract class ShuttleDataDb : RoomDatabase(), ShuttleDatabase {
    override val parcelDataAccessObject: ShuttleDataAccessObject by lazy {
        ShuttleDao(getParcelDao())
    }

    internal abstract fun getParcelDao(): ShuttleDao.Dao

    companion object {
        @Volatile
        private var INSTANCE: ShuttleDataDb? = null

        fun getInstance(context: Context): ShuttleDataDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): ShuttleDataDb {
            return Room.databaseBuilder(
                context.applicationContext,
                ShuttleDataDb::class.java,
                DB_NAME
            ).build()
        }
    }
}