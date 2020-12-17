package com.grarcht.room

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.grarcht.persistence.ShuttleDataAccessObject
import com.grarcht.persistence.ShuttleDataModel

class ShuttleDao(internal var dao: Dao) : ShuttleDataAccessObject {
  //  lateinit var dao: Dao

    override fun getParcelById(lookupKey: String): LiveData<ShuttleDataModel> {
        @Suppress("UNCHECKED_CAST")
        return dao.getParcelById(lookupKey) as LiveData<ShuttleDataModel>
    }

    override fun insertParcel(data: ShuttleDataModel): Long {
        return if (data is ShuttleData) {
            dao.insertParcel(data)
        } else {
            NOT_INSERTED_INCORRECT_TYPE
        }
    }
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    override fun insertParcelArray(data: Array<ShuttleDataModel>): Long {
//        return if (data.first() is ShuttleData) {
//            @Suppress("UNCHECKED_CAST")
//            dao.insertParcelArray(data as Array<ShuttleData>)
//        } else {
//            NOT_INSERTED_INCORRECT_ELEMENT_TYPE
//        }
//    }
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    override fun insertParcelList(data: ArrayList<ShuttleDataModel>): Long {
//        return if (data.first() is ShuttleData) {
//            @Suppress("UNCHECKED_CAST")
//            dao.insertParcelList(data as ArrayList<ShuttleData>)
//        } else {
//            NOT_INSERTED_INCORRECT_ELEMENT_TYPE
//        }
//    }

    override fun deleteAllParcelData() = dao.deleteAllParcelData()

    @androidx.room.Dao
    interface Dao {
        @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_LOOKUP_KEY = :lookupKey")
        fun getParcelById(lookupKey: String): LiveData<ShuttleData>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertParcel(data: ShuttleData): Long

//        @Insert(onConflict = OnConflictStrategy.REPLACE)
//        fun insertParcelArray(data: Array<ShuttleData>): Long
//
//        @Insert(onConflict = OnConflictStrategy.REPLACE)
//        fun insertParcelList(data: ArrayList<ShuttleData>): Long

        @Query("DELETE FROM $TABLE_NAME")
        fun deleteAllParcelData()
    }

    companion object {
        const val NOT_INSERTED_INCORRECT_TYPE: Long = -1
        const val NOT_INSERTED_INCORRECT_ELEMENT_TYPE: Long = -2
    }
}