package com.grarcht.shuttle.framework.integrations.extensions.room

import org.junit.Test
import org.junit.jupiter.api.Assertions

/**
 * Verifies the [ShuttleRoomDataModelFactory] functionality.
 */
class ShuttleRoomDataModelFactoryTest {
    private val modelFactory = ShuttleRoomDataModelFactory()

    @Test
    fun verifyCreationOfTheDb() {
        val model = modelFactory.createDataModel(cargoId = "cargoId1", filePath = "app/cargo/cargoId1")
        Assertions.assertNotNull(model)
    }
}
