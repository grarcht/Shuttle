package com.grarcht.shuttle.framework.integrations.extensions.room

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject.Companion.STORE_CARGO_FAILED
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModel


/**
 * This data access object class takes inspiration from the Decorator and Template Design
 * Patterns to decorate the Room Dao object with a constant to represent the insertion
 * fail state and to ensure the behavior the Room Dao object performs the what is expected
 * for [ShuttleDataAccessObject]s.  For more information on these design patterns, refer to:
 * <a href="https://www.tutorialspoint.com/design_pattern/decorator_pattern.htm">Decorator Design Pattern</a>
 * <a href="https://www.tutorialspoint.com/design_pattern/template_pattern.htm">Template Design Pattern</a>
 * <a href="https://www.tutorialspoint.com/design_pattern/data_access_object_pattern.htm">
 *     Data Access Object (DAO) Design Pattern</a>
 * @param dao the object ot decorate
 */
open class ShuttleRoomDao(private var dao: Dao) : ShuttleDataAccessObject {

    /**
     * Gets the number of cargo items
     * @return the number of cargo items
     */
    override fun getNumberOfCargoItems(): Int {
        return dao.getNumberOfCargoItems()
    }

    /**
     * This function returns the [Channel] of type [ShuttleDataModel] for the corresponding [cargoId].
     * @param cargoId the key used to get the [ShuttleDataModel]
     * @return the [Channel] used to get the [ShuttleDataModel] corresponding to the [cargoId].
     */
    override suspend fun getCargoBy(cargoId: String): ShuttleDataModel? {
        return dao.getCargoById(cargoId)
    }

    /**
     * Inserts a [ShuttleDataModel] into the database.
     * @param data the reference for the [ShuttleDataModel] object to insert
     * @return true if inserted
     */
    override fun insertCargo(data: ShuttleDataModel): Long {
        return if (data is ShuttleRoomData) {
            dao.insertCargo(data)
        } else {
            STORE_CARGO_FAILED
        }
    }

    /**
     * Deletes a data by the [cargoId].
     */
    override fun deleteCargoBy(cargoId: String): Int {
        return dao.deleteCargoBy(cargoId)
    }

    /**
     * Deletes all of the data in the database.
     */
    override fun deleteAllCargoData(): Int {
        return dao.deleteAllCargoData()
    }

    /**
     * The Room database interface for the data access object to generate.
     */
    @androidx.room.Dao
    interface Dao {
        /**
         * Gets the number of cargo items
         * @return the number of cargo items
         */
        @Query("SELECT Count(*) FROM $TABLE_NAME")
        fun getNumberOfCargoItems(): Int

        /**
         * This function returns the [LiveData] of type [ShuttleRoomData] for the corresponding [cargoId].
         * @param cargoId the key used to get the [ShuttleRoomData]
         * @return the [LiveData] used to get the [ShuttleRoomData] corresponding to the [cargoId].
         */
        @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_CARGO_ID = :cargoId")
        suspend fun getCargoById(cargoId: String): ShuttleRoomData

        /**
         * Inserts a [ShuttleRoomData] object into the database.
         * @param data the reference for the [ShuttleRoomData] object to insert
         * @return the value for the insertion (success or failure)
         */
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertCargo(data: ShuttleRoomData): Long

        /**
         * Deletes all of the data in the database.
         * @param cargoId for the cargo to delete
         */
        @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_CARGO_ID = :cargoId")
        fun deleteCargoBy(cargoId: String): Int

        /**
         * Deletes all of the data in the database.
         */
        @Query("DELETE FROM $TABLE_NAME")
        fun deleteAllCargoData(): Int
    }
}
