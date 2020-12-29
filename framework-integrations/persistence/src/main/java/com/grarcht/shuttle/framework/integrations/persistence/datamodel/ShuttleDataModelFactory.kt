package com.grarcht.shuttle.framework.integrations.persistence.datamodel

import android.os.Parcelable

/**
 *  This factory creates [ShuttleDataModel] objects from [Parcelable] objects.  For more
 *  information on the factory design pattern, refer to:
 *  <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">
 *      Factory Design Pattern</a>
 */
interface ShuttleDataModelFactory {

    /**
     * Creates [ShuttleDataModel] objects from [Parcelable] objects.
     * @param cargoId used to retrieve the [filePath] from the database
     * @param filePath where [ShuttleDataModel] will be persisted at
     * @return the reference to the newly created [ShuttleDataModel] object
     */
    fun createDataModel(cargoId: String, filePath: String): ShuttleDataModel
}
