package com.grarcht.shuttle.framework.result

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

private const val CARGO_ID = "cargoId1"

class ShuttleResultTests {

    @Test
    fun verifyNotPickingUpCargoYetIsInstantiable() {
        val result = ShuttlePickupCargoResult.NotPickingUpCargoYet
        assertNotNull(result)
    }

    @Test
    fun verifyNotRemovingCargoYetHoldsCargoId() {
        val result = ShuttleRemoveCargoResult.NotRemovingCargoYet(CARGO_ID)
        assertNotNull(result)
        assertEquals(CARGO_ID, result.cargoId)
    }

    @Test
    fun verifyNotStoringCargoYetHoldsCargoId() {
        val result = ShuttleStoreCargoResult.NotStoringCargoYet(CARGO_ID)
        assertNotNull(result)
        assertEquals(CARGO_ID, result.cargoId)
    }
}
