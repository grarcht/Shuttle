package com.grarcht.shuttle.framework.integrations.extensions.room

import android.os.Parcelable
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModel
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModelFactory

/**
 * This factory is used to create [ShuttleDataModel]s from blobs.  This class uses the
 * factory design pattern.  For more information on this design
 * pattern, refer to: <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">
 *     Factory Design Pattern</a>
 */
open class ShuttleRoomDataModelFactory : ShuttleDataModelFactory {

    /**
     * Creates [ShuttleDataModel] objects from [Parcelable] objects.
     * @param cargoId used to retrieve the [filePath] from the database
     * @param filePath where [ShuttleDataModel] will be persisted at
     * @return the reference to the newly created [ShuttleDataModel] object
     */
    override fun createDataModel(cargoId: String, filePath: String): ShuttleDataModel {
        return ShuttleRoomData(cargoId, filePath)
    }
}
