package com.grarcht.shuttle.framework.integrations.extensions.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModel

// The column for the key used to look up corresponding blobs
const val COLUMN_CARGO_ID = "cargo_id"

const val COLUMN_FILEPATH = "file_path"

// The name of the table for storing data blobs
const val TABLE_NAME = "shuttle"

/**
 * The data model for the table entries.
 */
@Entity(tableName = TABLE_NAME)
open class ShuttleRoomData(
    /**
     * Used to retrieve the blobs from the database.
     */
    @PrimaryKey
    @ColumnInfo(name = COLUMN_CARGO_ID, typeAffinity = ColumnInfo.TEXT)
    override var cargoId: String,

    @ColumnInfo(name = COLUMN_FILEPATH, typeAffinity = ColumnInfo.TEXT)
    override var filePath: String = ""
) : ShuttleDataModel {
    constructor() : this("", "")
}
