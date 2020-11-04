package com.grarcht.persistence

import androidx.lifecycle.LiveData

interface ShuttleDataAccessObject {
    fun getParcelById(lookupKey: String): LiveData<ShuttleDataModel>

    fun insertParcel(data: ShuttleDataModel): Long

//    fun insertParcelArray(data: Array<ShuttleDataModel>): Long
//
//    fun insertParcelList(data: ArrayList<ShuttleDataModel>): Long

    fun deleteAllParcelData()
}