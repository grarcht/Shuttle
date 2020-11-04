package com.grarcht.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grarcht.persistence.ShuttleDataModel
import java.util.UUID

const val AUTO_GENERATE = true
const val COLUMN_ID = "_id"
const val COLUMN_LOOKUP_KEY = "lookup_key"
const val COLUMN_BLOB = "blob"
const val INITIAL_ID = 0
const val TABLE_NAME = "shuttle"

@Entity(tableName = TABLE_NAME)
open class ShuttleData(
    @ColumnInfo(name = COLUMN_LOOKUP_KEY, typeAffinity = ColumnInfo.TEXT)
    override var lookupKey: String,
    @ColumnInfo(name = COLUMN_BLOB, typeAffinity = ColumnInfo.BLOB)
    override var data: ByteArray
) : ShuttleDataModel {
    @PrimaryKey(autoGenerate = AUTO_GENERATE)
    @ColumnInfo(name = COLUMN_ID, typeAffinity = ColumnInfo.INTEGER)
    override var parcelId: Int = INITIAL_ID
}
